package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManagerService;
import org.apache.http.protocol.HTTP;
import org.springframework.stereotype.Component;
import sun.nio.cs.US_ASCII;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    //定义购物车名称
    private String cookieCartName = "CART";
    //设置cookie过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManagerService managerService;

    /**
     * 未登录的时候，添加到购物车
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response,String skuId, String userId,Integer skuNum){
        //判断cookie中是否有购物车，有可能有中文，所以要进行序列化
        //该字符串是购物车全部信息
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist = false;            //做一个标记 默认为false
        if(cartJson!=null){
            cartInfoList= JSON.parseArray(cartJson,CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    //如果找到指定的商品，在此基础上添加商品数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //价格设置
                    cartInfo.setSkuPrice(cartInfo.getSkuPrice());
                    ifExist=true;
                    break;
                }

            }
        }

        //购物车里没有对应的商品或者没有购物车
        if (!ifExist){
            //把商品信息取出来，新增到购物车
            SkuInfo skuInfo =managerService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);     //将完成的商品信息实例再存到集合中

        }
        //将购物车信息序列化写入cookie
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
    }


    /**
     * 从cookie中查询购物车列表
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {

        //通过用户请求，将得到指定用户id的购物车列表字符串
        String cartJson = CookieUtil.getCookieValue(request,cookieCartName,true);
        //将购物车列表字符串转换为List集合
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson,CartInfo.class);
        return cartInfoList;
    }

    /**
     * 删除cookie中的购物车
     * @param request
     * @param response
     */
    public  void  deleteCartCookie(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    /**
     * Cookie中的查询勾选状态方法
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        //循环比较
        for (CartInfo cartInfo : cartList) {
            if(cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }

        }
        //保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        //调用工具类方法
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }
}
