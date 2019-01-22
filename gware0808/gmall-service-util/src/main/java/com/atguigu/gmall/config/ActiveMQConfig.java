package com.atguigu.gmall.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

/**
 *activeMQ--配置类
 */
@Configuration
public class ActiveMQConfig {



    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL ;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;
    /*
        <beans>
            <bean id="activeMQUtil" class="com.atguigu.gmall0808.config.ActiveMQUtil">
            </bean>
            <bean id="jmsQueueListener" class="org.springframework.jms.config.DefaultJmsListenerContainerFactory">
                <property name="connectionFactory" ref="activeMQConnectionFactory"> </property>
            </bean>
            <bean id="activeMQConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
                <property name="brokerURL" value="tcp://192.168.67.211:61616"></property>
            </bean>
        </beans>
     */

    //调用初始化方法赋值，得到ActiveMQUtil对象
    @Bean
    public ActiveMQUtil getActiveMQUtil(){

        if("disabled".equals(brokerURL)){
            return  null;

        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.init(brokerURL);
        return  activeMQUtil;
    }

    //消息监听器的工厂
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory){
        if("disabled".equals(listenerEnable)){
            return  null;

        }

        //消息监听器工厂
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        //将监听的工厂赋给监听器--监听指定的工厂对象
        factory.setConnectionFactory(activeMQConnectionFactory);
        //设置事务
        factory.setSessionTransacted(false);
        //自动签收
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        //设置并发数
        factory.setConcurrency("5");
        //重连间隔时间
        factory.setRecoveryInterval(5000L);
        return factory;
    }

    //接收消息
    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(){
        ActiveMQConnectionFactory activeMQConnectionFactory  = new ActiveMQConnectionFactory(brokerURL);
        return activeMQConnectionFactory;
    }

}
