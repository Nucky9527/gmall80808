package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    //查询所有用户信息
    List<UserInfo> findAll( );

    //查询用户的地址信息
    List<UserAddress> getUserAddressList(String userId);

    //用户登录
    UserInfo login(UserInfo userInfo);


    /**
     * 验证用户是否登录
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
