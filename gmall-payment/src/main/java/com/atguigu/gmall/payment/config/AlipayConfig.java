package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// 读取alipay.properties
@Configuration
@PropertySource("classpath:alipay.properties")
public class AlipayConfig {

    // 读取配置文件中的数据
    @Value("${alipay_url}")
    private String alipay_url;

    @Value("${app_private_key}")
    private String app_private_key;

    @Value("${app_id}")
    private String app_id;


    public final static String format="json";
    public final static String charset="utf-8";
    public final static String sign_type="RSA2";


    public static String return_payment_url;

    public static  String notify_payment_url;

    public static  String return_order_url;

    public static  String alipay_public_key;
    // 从配置文件中读取数据 MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhkZi6W0wn/prX+NIIF9ATb5Z8ReKK4hFYtBrweDfGHD1mNW7YIZY4G5hE7S2Sry8eFXlFgSlBWlJ4fVnDaK9MkVThpwE2H65ooVlK/wLuyPqovIVpMt/utva5Ayuzv7eQOWK45FdLDNDlK8QLoBko6SS+YbnWnf7a+mrf4NAS4UFClpfe8Byqe8XIraO2Cg4Ko5Y5schX39rOAH8GlLdgqQRYVQ2dCnkIQ+L+I4Cy9Mvw3rIkTwt3MBU+AqREXY4r5Bn6cmmX/9MAJbFqrofGiUAqG+qbjTcZAzgNPfuiD0zXgt/YYjMQMzck75BOmwnYOam2ajODUSQn8Xybsa7wQIDAQAB
    @Value("${alipay_public_key}")
    public   void setAlipay_public_key(String alipay_public_key) {
        AlipayConfig.alipay_public_key = alipay_public_key;
    }

    @Value("${return_payment_url}")
    public   void setReturn_url(String return_payment_url) {
        AlipayConfig.return_payment_url = return_payment_url;
    }

    @Value("${notify_payment_url}")
    public   void setNotify_url(String notify_payment_url) {
        AlipayConfig.notify_payment_url = notify_payment_url;
    }

    @Value("${return_order_url}")
    public   void setReturn_order_url(String return_order_url) {
        AlipayConfig.return_order_url = return_order_url;
    }

    @Bean
    public AlipayClient alipayClient(){
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayClient alipayClient=new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset, alipay_public_key,sign_type );
        return alipayClient;
    }

}

