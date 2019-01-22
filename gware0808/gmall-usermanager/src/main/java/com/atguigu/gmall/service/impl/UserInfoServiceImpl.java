package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.mapper.UserAddressMapper;
import com.atguigu.gmall.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;


    public String USERKEY_PREFIX="user:";
    public String USERINFOKEY_SUFFIX=":info";
    public int USERKEY_TIMEOUT=60*60*24;



    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        List<UserAddress> addressList = userAddressMapper.select(userAddress);
        return addressList;
    }


    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //先给传递过来的用户密码加密
      String passwd= userInfo.getPasswd();
      String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
      userInfo.setPasswd(newPassword);
        //从数据库查询得到返回的用户信息
        UserInfo info = userInfoMapper.selectOne(userInfo);
        //判断是否为空，若不为空，则将用户信息存储到redis中
        if(info!=null){
            Jedis jedis =redisUtil.getJedis();
            String userKey = USERKEY_PREFIX+info.getId()+USERINFOKEY_SUFFIX;
            jedis.setex(userKey,USERKEY_TIMEOUT,JSON.toJSONString(info));
            jedis.close();
        }

        return info;
    }

    /**
     * 验证用户是否登录
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId) {
        //获取Jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String userKey = USERKEY_PREFIX+userId+USERINFOKEY_SUFFIX;
        //取出数据
        String userJson = jedis.get(userKey);
        //延长用户的过期时间
        jedis.expire(userKey,USERKEY_TIMEOUT);
        //判断当前是否有数据
        if(userJson!=null && !"".equals(userJson)){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }


}
