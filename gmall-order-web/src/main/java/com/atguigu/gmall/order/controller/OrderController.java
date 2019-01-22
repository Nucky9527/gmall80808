package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    //@Autowired
    @Reference
    UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    /**
     * 测试方法
     *
     * @param userId
     * @return
     */
    @RequestMapping("trade")
    @ResponseBody
    public List<UserAddress> trade(String userId) {
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        return userAddressList;
    }


    /**
     * 初始化订单----已登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "trade", method = RequestMethod.GET)
    @LoginRequire
    public String tradeInit(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        //得到选中的购物车列表
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        //得到收获人地址
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        request.setAttribute("userAddresslist", userAddressList);
        //订单信息集合---被选中的购物车列表的大小
        List<OrderDetail> orderDetailList = new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        //将实体对象存入作用域中
        request.setAttribute("orderDetailList", orderDetailList);
        //然后创建订单对象
        OrderInfo orderInfo = new OrderInfo();
        //将订单详情赋值给订单实例对象
        orderInfo.setOrderDetailList(orderDetailList);
        //计算总价
        orderInfo.sumTotalAmount();
        //将价格存入作用域中
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        //获取TradeCode号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode", tradeNo);

        //跳转到指定页面
        return "trade";


    }


    /**
     * 结算订单
     *
     * @param orderInfo
     * @param request
     * @return
     */
    @RequestMapping(value = "submitOrder", method = RequestMethod.POST)
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, org.apache.catalina.servlet4preview.http.HttpServletRequest request) {
        //检查
        String userId = (String) request.getAttribute("userId");

        //检查tradeCode
        String tradeNo = request.getParameter("tradeNo");

        //验证流水号
        boolean flag = orderService.checkTradeCode(userId, tradeNo);

        //判断
        if (!flag) {
            request.setAttribute("errMsg", "该页面已失效，请重新结算！");
            //跳转到失败页面
            return "tradeFail";
        }

        //初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        //校验，验价
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {

            //从订单中取购物skuId，数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result) {
                request.setAttribute("errMsg", "商品库存不足，请重新下单！");
                return "tradeFail";
            }


        }

        //保存---返回一个id
        String orderId = orderService.saveOrder(orderInfo);
        //删除tradeNo
        orderService.delTradeCode(userId);
        //重定向
        return "redirect://payment.gmall.com/index?orderId=" + orderId;

    }
}
