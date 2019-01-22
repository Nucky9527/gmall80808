package com.atguigu.gmall.passport;

import com.atguigu.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {


	public void contextLoads() {
	}

	@Test
	public void testJWT(){
		String key = "atguigu";
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("userId",1001);
		map.put("nickName","Admin");
		String salt = "192.168.203.1";
		String token = JwtUtil.encode(key,map,salt);		//编码
		System.out.println("token:"+token);
		Map<String, Object> decode = JwtUtil.decode(token, key, salt);		//解码
		System.out.println("decode"+decode);
	}

}

