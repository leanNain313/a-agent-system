package com.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ye.mapper")
@SpringBootApplication
public class AiAgentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentSystemApplication.class, args);
    }

}
