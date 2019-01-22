package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PublicKey;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private CartService cartService;

    @Reference
    private ManagerService managerService;


    /**
     * 添加到购物车----登录与未登录
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        //获取userID ，skuId，skuNum
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");

        //判断用户是否登录--根据userId
        if(userId!=null){
            //说明用户登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            //说明用户没有登录， 没有登录则将信息保存到cookie中
        cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));

        }
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    /**
     * 购物车列表-------登录与未登录
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //判断用户是否登录，登录了从redis中取，redis中没有，从数据库中取
        //没有登录的时候，从cookie中取
        String userId = (String) request.getAttribute("userId");
        //判断
        if(userId != null){
            //从cookie中查询购物车信息
            List<CartInfo> cartListFromCookie= cartCookieHandler.getCartList(request);
            List<CartInfo> cartList = null;
            if(cartListFromCookie!=null && cartListFromCookie.size()>0){
                //开始合并
                cartList=cartService.mergeToCartList(cartListFromCookie,userId);
                //合并完之后，删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                //从redis中取得，或者从数据库中
                cartList = cartService.getCartList(userId);
            }
            request.setAttribute("cartList",cartList);

        }else{
            List<CartInfo>cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";




    }


    /**
     * 检查购物车记录是否被选中------登录与未登录
     * @param request
     * @param response
     */
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");
        //判断是否为空

        /**
         * 这里同样要区分，用户登录和未登录状态
         * 如果登录，修改缓存中的数据，如果未登录，修改cookie中的数据
         */
        if(userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    /**
     * 要解决用户在未登录且购物车中有商品的情况下，直接点击结算
     * 所以不能直接跳转到结算页面，要让用户强制登录后，检查cookie并进行合并后再重定向到结算页面。
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect =true )
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //从前台获取到用户id
        String userId = (String) request.getAttribute("userId");
        //获取cookie中指定user的购物车信息
        List<CartInfo> cookieHanderCartList = cartCookieHandler.getCartList(request);
        //判断cookie中的购物车信息是否为空
        if(cookieHanderCartList!=null && cookieHanderCartList.size()>0){
            //合并购物车，此时用户已登录
            cartService.mergeToCartList(cookieHanderCartList,userId);
            //合并之后清空cookie中的信息
            cartCookieHandler.deleteCartCookie(request,response);
        }
        //重定向到指定页面
        return "redirect://order.gmall.com/trade";
    }



}
