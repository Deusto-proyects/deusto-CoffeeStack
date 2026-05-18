package com.deusto.coffeestack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CoffeeStackApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoffeeStackApplication.class, args);
    }
}
