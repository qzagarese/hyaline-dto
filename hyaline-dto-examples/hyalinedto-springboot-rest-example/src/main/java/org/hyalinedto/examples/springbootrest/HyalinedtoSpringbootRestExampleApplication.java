package org.hyalinedto.examples.springbootrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class HyalinedtoSpringbootRestExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyalinedtoSpringbootRestExampleApplication.class, args);
    }
}
