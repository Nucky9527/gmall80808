package com.atguigu.gmall.order.mq;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        System.out.println("result="+result);
        System.out.println("orderId="+orderId);
        if("success".equals(result)){
            //更新支付状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //通知减库存
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);

        }else {         //通知支付失败的情况

            orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
        }
    }

}
