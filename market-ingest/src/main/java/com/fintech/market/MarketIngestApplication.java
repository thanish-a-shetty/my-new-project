package com.fintech.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketIngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketIngestApplication.class, args);
    }
}
