package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 获取购物车列表，通过用户的id
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 数据库中查询
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId);

    /**
     * 合并cookie和数据库中的购物车信息
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    /**
     * 检查购物车是否被选中
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 获取被选中的购物车
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
