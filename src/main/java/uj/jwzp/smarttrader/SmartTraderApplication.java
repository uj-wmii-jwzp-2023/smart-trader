package uj.jwzp.smarttrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartTraderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartTraderApplication.class, args);
    }

}