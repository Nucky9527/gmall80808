package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.mapper")
@ComponentScan("com.atguigu.gmall")
public class GmallUsermanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanagerApplication.class, args);
	}

}

