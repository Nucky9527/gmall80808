package com.atguigu.gmall.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
/**
 * ActiveMQ的工具类--与springboot进行整合
 */
public class ActiveMQUtil {

    //连接池工厂
    PooledConnectionFactory pooledConnectionFactory = null;

    public  void  init(String brokerUrl){
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
         pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
         //设置超时时间
        pooledConnectionFactory.setReconnectOnException(true);
        //设置出现异常的时候，继续重试链接
        pooledConnectionFactory.setMaxConnections(5);

    }

    //获取链接
    public Connection getConnection(){
        Connection connection = null;
        try {
            //连接池对象创建链接
            connection = pooledConnectionFactory.createConnection();
        }catch (JMSException e){
            e.printStackTrace();
        }
        return connection;
    }

}
