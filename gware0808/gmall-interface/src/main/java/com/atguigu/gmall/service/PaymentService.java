package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {

    /**
     * 保存交易记录
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 通过out_trade_no查询数据库中的交易记录实体对象
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no 更改交易记录状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);


    /**
     * 发送支付结果的消息
     * @param paymentInfo
     * @param result
     */
    public void sendPaymentResult(PaymentInfo paymentInfo,String result);
}
