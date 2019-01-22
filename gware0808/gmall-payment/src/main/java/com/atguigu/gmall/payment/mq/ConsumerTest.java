package com.atguigu.gmall.payment.mq;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * 消息的消费端
 */
public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
        //创建一个消息队列工厂
        ActiveMQConnectionFactory  activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER, ActiveMQConnectionFactory.DEFAULT_PASSWORD, "tcp://192.168.203.128:61616");
        //创建链接
        Connection connection = activeMQConnectionFactory.createConnection();
        //打开链接
        connection.start();
        //创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("atguigu");
        //创建消费者
        MessageConsumer consumer = session.createConsumer(queue);
        //消费信息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                //判断传过来的对象是否是该类下的实例
                if(message instanceof ActiveMQTextMessage){
                    try{
                        String text = ((ActiveMQTextMessage)message).getText();
                    }catch (JMSException e){
                        e.printStackTrace();
                    }
                }
            }
        });



    }
}
