package com.leafvision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.leafvision.mapper")
@EnableScheduling
public class LeafVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeafVisionApplication.class, args);
    }
}
