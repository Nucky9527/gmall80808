package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;


public interface OrderService{

    /**
     * 返回orderId，保存完，应该调到支付，根据orderId
     */
    public  String  saveOrder(OrderInfo orderInfo);

    /**
     * 生成一个字符串
     * @param userId--将字符串保存到redis时，需要userId作为key
     * @return
     */
    String getTradeNo(String userId);

    /**
     * tradeCode比较
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除redis中的，tradeCode
     * @param userId
     */
    void delTradeCode(String userId);


    /**
     * 查询库存系统
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId,Integer skuNum);


    /**
     * 通过订单的id，得到指定的订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 发送订单状态给库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);
}
