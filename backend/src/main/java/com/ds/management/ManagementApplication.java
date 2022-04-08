package com.ds.management;

import com.ds.management.util.UDPSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(ManagementApplication.class);

    public static void main(String[] args) {

        try {
            ConfigurableApplicationContext appContext = SpringApplication.run(ManagementApplication.class, args);
            UDPSocketListener udpSocketListener = appContext.getBean(UDPSocketListener.class);
            CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    udpSocketListener.listen();
                }
            });
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
