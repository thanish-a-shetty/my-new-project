package com.fintech.sip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SipServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipServiceApplication.class, args);
    }
}
