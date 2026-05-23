package com.echoowl.backend;

import com.echoowl.backend.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EchoOwlBackendApplication {
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(EchoOwlBackendApplication.class, args);
    }
}
