package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


/**
 * 自定义拦截器（第一个拦截器，可能后续还有其他拦截器）
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //进入控制器之前------使用这个方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取new token(从前端验证，只有在登录成功之后，才能取得到newToken)
        String token = request.getParameter("newToken");
        //判断
        if(token!=null){
            //将token放入cookie中,将使用工具类
           // Cookie cookie = new Cookie("token", token);
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //注意：该条件表示的是：当用户登录之后，而用户又去访问不需要登录的页面时。
        if(token==null){
            token=CookieUtil.getCookieValue(request,"token",false);
        }

        //取得token然后做解码得到用户的昵称
        if(token!=null){
            //解码token,想办法获取第二部分
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            //将用户昵称保持到作用域
            request.setAttribute("nickName",nickName);
        }

        //需要知道方法上是否有该注解？handler转换为方法类
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            //有注解可能需要登录？需要调用认证方法
            //先获取currentIp，服务器的Ip地址
            String currentIp = request.getHeader("X-forwarded-for");
            //调用认证方法verify,需要token，currentIp参数都有了，但是不在同一个项目中，跨项目调用--HttpClientUtil
         String result =  HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS+"?token="+token+"&currentIp="+currentIp);
           //判断
           if("success".equals(result)){
               //认证成功！保存一个用户id---后续购物车会使用
               Map map = getUserMapByToken(token);
               String userId = (String)map.get("userId");
               request.setAttribute("userId",userId);
               //认证成功，放行！
               return true;
           }else{
               //反之，认证失败，说明没有登录
               //需要不需要登录主要看autoRedirect（）是否是true
               if(methodAnnotation.autoRedirect()){
                   //这里必须登录!跳转到登录页面
                   //获取当前原始的url
                   String requestURL = request.getRequestURL().toString();
                   //将原始的url进行编码
                   String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                   //跳转到登录页面
                   response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                   //认证失败！
                   return false;
               }
           }
            //在项目中可能有多个拦截器，只有第一个拦截器return true，才会走第二个拦截器
            //所以在写第一个拦截器的时候，不管后面还有没有拦截器，都要写上return true 和 return false。

        }

        return true;
    }

    /**
     * 得到token第二部分
     * @param token
     * @return
     */
    private Map getUserMapByToken(String token) {

        String tokenUserInfo=StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();       //新的解码方式
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        String userStr = null;
        //将字节数组转换为字符串
        try{
             userStr = new String(bytes,"UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        //字符串转换为map
        Map map = JSON.parseObject(userStr, Map.class);
        return map;
    }


    //进入控制器之后，返回视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //视图渲染之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
