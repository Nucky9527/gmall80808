package com.atguigu.gmall.payment.service.impl;



import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;


    /**
     * 保存交易记录
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    /**
     * 通过out_trade_no查询数据库中的交易记录实体对象
     * @param paymentInfo
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return  paymentInfoMapper.selectOne(paymentInfo);
    }


    /**
     * 根据out_trade_no 更改交易记录状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {
        // update paymentInfo set PaymentStatus = PAID and callbackTime = new Date() where out_trade_no = out_trade_no
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd,example);

    }


    /**
     * 发送消息，给activeMQ支付结果，success/fail
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        //获取链接
        Connection connection = activeMQUtil.getConnection();
        try{
            //打开链接
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");

            //创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);

            //创建消息对象
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);
            //准备发送消息
            producer.send(mapMessage);
            //提交发送的内容
            session.commit();
            //关闭--范围从小到大
            producer.close();
            session.close();
            connection.close();
        }catch (JMSException e){
            e.printStackTrace();
        }


    }
}
