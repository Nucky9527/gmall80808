package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.config.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${token.key}")
    private String key;

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    /**
     * 用户登录
     * @param userInfo
     * @param request
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        //String salt = "192.168.203.1";
        String salt = request.getHeader("X-forwarded-for");
        //调用服务层方法
       UserInfo info=userInfoService.login(userInfo);
        if(info!=null){
            HashMap<String,Object> map = new HashMap<String, Object>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            String token = JwtUtil.encode(key,map,salt);		//编码
            System.out.println("token:"+token);
            return token;
        }else{
            return "fail";
        }

    }

    /**
     * 验证用户是否登录
     * @param request
     * @return
     */
    @RequestMapping("verify")       //verify---校验
    @ResponseBody
    public String verify(HttpServletRequest request){
        //先获取token,salt
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        //然后解码
        Map<String,Object> map = JwtUtil.decode(token,key,currentIp);
        if(map!=null && map.size()>0){
            //取出userId
            String userId = (String)map.get("userId");
            //调用校验的方法
           UserInfo userInfo= userInfoService.verify(userId);
           if(userInfo!= null){
               return "success";

           }else {
               return "fail";
           }
        }
        return  "fail";
    }

}
