package com.rameo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TODO
 *
 * @author suwei
 * @version 1.0
 * @date 2022/1/13 21:01
 */
@SpringBootApplication
@EnableScheduling
public class JsoupCrawlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JsoupCrawlerApplication.class, args);
    }
}
