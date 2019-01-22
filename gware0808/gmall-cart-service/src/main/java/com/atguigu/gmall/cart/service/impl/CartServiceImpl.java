package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 思路：先检查该用户的购物车里是否已经有该商品
 * 如果有商品，只要把对应商品的数量增加上去就可以，同时更新缓存
 * 如果没有商品，则把对应商品插入到购物车中，同时插入缓存
 *
 */
@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManagerService managerService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 添加到购物车中
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        //先查数据库里是否有指定的购物车信息
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        //select * from cartInfo where skuId = ? and userId = ?;
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        //判断
        if(cartInfoExist!=null) {
            //更新商品数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            //给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            SkuInfo skuInfo = managerService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            //插入数据库
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }
        //构建 userCartKey = key : userId : cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //准备取数据
        Jedis jedis = redisUtil.getJedis();
        //将对象序列化
        String cartJson = JSON.toJSONString(cartInfoExist);
        jedis.hset(userCartKey,skuId,cartJson);
        //更新购物车过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }


    /**
     * 通过用户id，查询购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        //从redis中取得
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartJsons = jedis.hvals(userCartKey);
        //判断
            if(cartJsons!=null && cartJsons.size()>0){
                List<CartInfo>cartInfoList = new ArrayList<>();
                for (String cartJson : cartJsons) {
                    CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
                    cartInfoList.add(cartInfo);
                }
                //排序
                cartInfoList.sort(new Comparator<CartInfo>() {
                    @Override
                    public int compare(CartInfo o1, CartInfo o2) {
                        return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                    }
                });

                return cartInfoList;
            }else {
                //redis中没有，从数据库中查询，其中cartPrice可能是旧值，所以需要关联skuInfo表信息
                List<CartInfo> cartInfoList = loadCartCache(userId);

                return cartInfoList;

            }


    }

    /**
     * 购物车查询，在数据库中查询
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> loadCartCache(String userId) {

        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        //判断
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String, String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),cartJson);
        }
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }


    /**
     * cookie和数据库中的购物车合并
     *
     * 思路：用数据库中的购物车列表与传递过来的cookie里的购物车列表循环匹配。
     * 能匹配上的数量相加，匹配不上的 插入到数据库中，最后再重新加载缓存。
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        //循环开始匹配
        for (CartInfo cartInfoCK : cartListFromCookie) {
            boolean isMatch = false;   //初始标记
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if(cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            //说明数据库中没有相对应的购物车,直接将cookie中购物车添加到数据库中
            if (!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //重新再数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if(cartInfo.getSkuId().equals(info.getSkuId())){
                    //只有被勾选的才会进行更改
                    if(info.getIsChecked().equals("1")){
                        //更新数据库的数据
                        cartInfo.setIsChecked(info.getIsChecked());
                        //并且更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }

            }

        }

        return cartInfoList;
    }

    /**
     * 检车购物车勾选状态--已登录
     *再保存回redis中
     * 同时保存另一个redis的key专门用来存储用户选中的商品，方便结算页面使用。
     * @param skuId
     * @param isChecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        //更新购物车的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        //定义一个key
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //取得购物车中的信息
        String cartJson = jedis.hget(userCartKey, skuId);
        //将cartJson转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        //为对象中的属性赋值
        cartInfo.setIsChecked(isChecked);
        //再将对象转换为json串
        String cartCheckedJson = JSON.toJSONString(cartInfo);
        //将新数据保存到redis中
        jedis.hset(userCartKey,skuId,cartCheckedJson);
        //新增到已选中的购物车
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        //判断选中状态
        if(isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckedJson);
        }else {
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();

    }


    /**
     * 在订单页面中获取的被选中的购物车列表信息
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //获得redis中的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        //通过key获取指定的对象--集合
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        //先创建一个集合对象
        List<CartInfo> newCartList = new ArrayList<>();
        //遍历集合
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;
    }

}
