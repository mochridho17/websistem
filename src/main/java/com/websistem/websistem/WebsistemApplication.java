// filepath: d:\RIDHO\Springboot project\websistem\src\main\java\com\websistem\websistem\WebsistemApplication.java
package com.websistem.websistem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebsistemApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsistemApplication.class, args);
    }
}