package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过订单要执行该控制器中的方法
 */
@Controller
public class PaymentController {

        @Reference
        private OrderService orderService;

        @Autowired
        private PaymentService paymentService;

        @Autowired
        AlipayClient alipayClient;


    /**
     * 对于商家的异步回调---验证是否支付，关键看支付状态
     * @param paramMap
     * @param request
     * @return
     */
        @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
        @ResponseBody
        public String paymentNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request) throws AlipayApiException {

            //更改交易记录状态，更新PaymentStatus.PAID
            PaymentInfo paymentInfoUpd = new PaymentInfo();

                //先创建一个标记--调用sdk验证签名
            boolean flag = AlipaySignature.rsaCheckV1(paramMap,AlipayConfig.alipay_public_key,"utf-8",AlipayConfig.sign_type);
            //判断，标记为false
           if(flag){
               //验签：支付宝中有交易记录
               //验签成功后，按照支付结果异步通知中的描述，
               // 对支付结果中的业务内容进行二次校验，
               // 校验成功后在response中返回success并继续商户自身业务处理，
               // 校验失败返回failure。
               //获取trade_status交易状态
               String trade_status = paramMap.get("trade_status");
               //条件：此时验签已成功，二次校验，支付结果中的状态和交易记录表中的状态进行比较
               if("TRADE_SUCCESS".equals(trade_status)|| "TRADE_FINISHED".equals(trade_status)){
                   //如果交易记录中paymentInfo paymentStatus 状态为close 或者paid，此时返回应该是fail
                   //通过out_trade_no查询交易状态
                   String out_trade_no = paramMap.get("out_trade_no");
                   //select * from payment_info where out_trade_no = ?
                   //创建交易记录的实体对象
                   PaymentInfo paymentInfo = new PaymentInfo();
                   paymentInfo.setOutTradeNo(out_trade_no);
                   //查询交易记录的实体对象
                   PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                   //查询到交易记录实体对象的交易状态，如果是已支付，或者已关闭，就不能再二次支付，该订单已完成支付。所以返回fail
                   if(paymentInfoQuery.getPaymentStatus()==PaymentStatus.PAID || paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED){
                       return "fail";
                   }

                   paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                   paymentInfoUpd.setCallbackTime(new Date());
                   paymentInfoUpd.setSubject(paramMap.toString());
                   paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);


                   //验证完毕，发送消息
                   paymentService.sendPaymentResult(paymentInfoUpd,"success");

                   return "success";
               }


           }else{
               //验签的时候就已经失败了，记录异常日志，并在response中返回failure
               paymentService.sendPaymentResult(paymentInfoUpd,"fail");
               return "fail";
           }

           return "fail";
        }


    /**
     * 对于用户的同步回调---已支付
     * @return
     */
    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
        public String callbackReturn(){
            return "redirect:"+AlipayConfig.return_order_url;
        }


    /**
     * 根据用户选中的支付方式，生成对应的二维码
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
        @ResponseBody
        public String submitPayment(HttpServletRequest request, HttpServletResponse response){
            //先获取订单id
            String orderId = request.getParameter("orderId");
            //再获取订单信息
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);

            //通过订单信息，保存支付信息
            //创建支付信息的实例对象，并为其属性赋值
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderId(orderId);
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
            paymentInfo.setSubject(orderInfo.getTradeBody());
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

            //保存支付信息
            paymentService.savePaymentInfo(paymentInfo);

            //生成支付宝参数

            //先创建api对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
            alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回调和通知地址

            //声明一个Map
            HashMap<String, Object> bizContnetMap = new HashMap<>();
            //在map中存入支付宝需要的相关参数
            bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
            bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
            bizContnetMap.put("subject",paymentInfo.getSubject());
            bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
            //将map变成json
            String Json = JSON.toJSONString(bizContnetMap);
            alipayRequest.setBizContent(Json);
            String form="";

            try {
                form = alipayClient.pageExecute(alipayRequest).getBody();//调用sdk生成表单
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            //设置字符集编码
            response.setContentType("text/html;charset=UTF-8");
            return  form;
        }


    /**
     * 在订单页面上点击结算时跳转到结算页面
     * @param request
     * @return
     */
    @RequestMapping("index")
        @LoginRequire
    public String index(HttpServletRequest request){
            //获取订单的Id
            String orderId = request.getParameter("orderId");
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            request.setAttribute("orderId",orderId);
            request.setAttribute("totalAmount",orderInfo.getTotalAmount());

            return "index";

        }


    /**
     * 发送消息--测试验证
     * @param paymentInfo
     * @param result
     * @return
     */
    @RequestMapping("sendPaymentResult")
        @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return  "sentPaymentResult";
        }



}
