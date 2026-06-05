package com.reimagineafrica.dividend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaccosDividendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaccosDividendServiceApplication.class, args);
    }
}
