package com.atguigu.gmall.payment.mq;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.Command;

import javax.jms.*;

/**
 * 消息队列生产者--测试
 */
public class ProducerTest {

    public static void main(String[] args) throws JMSException {

        //创建一个消息队列工厂
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.203.128:61616");
        //创建链接
        Connection connection = activeMQConnectionFactory.createConnection();
        //打开链接
        connection.start();
        //创建session对象 第一个参数Boolean类型，表示事务是否开启，第二个参数需要根据第一个参数的设定而选择
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("atguigu");
        //创建一个消息提供者
        MessageProducer producer = session.createProducer(queue);
        //创建一个消息对象
        ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
        //设置发送的内容
        textMessage.setText("你好，世界！");
        //提交
        session.commit();
        //发送消息
        producer.send(textMessage);
        //关闭---范围从小到大
        producer.close();
        session.close();
        connection.close();

    }

}
