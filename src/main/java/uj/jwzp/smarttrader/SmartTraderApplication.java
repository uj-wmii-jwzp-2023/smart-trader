package uj.jwzp.smarttrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication
@EnableScheduling
public class SmartTraderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartTraderApplication.class, args);
    }

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

}