package com.ds.management;

import com.ds.management.util.UDPSocketListener;
import com.ds.management.util.UDPSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class ManagementApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ManagementApplication.class, args);
            UDPSocketListener udpSocketListener= new UDPSocketListener();
            CompletableFuture<Void> completableFuture= CompletableFuture.runAsync(udpSocketListener);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /*@Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/student").allowedOrigins("http://localhost:3000").all;
            }*/
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }
}
