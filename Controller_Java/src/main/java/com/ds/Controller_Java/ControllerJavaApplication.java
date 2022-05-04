package com.ds.Controller_Java;

import com.ds.Controller_Java.config.SocketConfig;
import com.ds.Controller_Java.model.Message;
import com.ds.Controller_Java.model.StudentRequest;
import com.ds.Controller_Java.util.ControllerUdpListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@SpringBootApplication
public class ControllerJavaApplication {
    private final static Logger LOGGER = Logger.getLogger(ControllerJavaApplication.class.getName());

    public static void main(String[] args) {
        byte[] buf = new byte[102400];
        ConfigurableApplicationContext appContext = SpringApplication.run(ControllerJavaApplication.class, args);
        SocketConfig socketConfig = appContext.getBean(SocketConfig.class);
        try {
            ControllerUdpListener controllerUdpListener = appContext.getBean(ControllerUdpListener.class);
            CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    controllerUdpListener.listen();
                }
            });
//            int count=0;
//            while(count<10) {
        /*    Thread.sleep(10000);
            DatagramSocket socket = socketConfig.socket();
            Message m = new Message();
            m.setSender_name("Controller");
            m.setKey("K1");
            m.setTerm(1);
            m.setRequest("STORE");

            StudentRequest s = new StudentRequest();
            s.setName("Anish");
            s.setAge(27);
            s.setCgpa(3.84);
            s.setStudentNumber("50419412");

            Gson gson = new Gson();
            String value = gson.toJson(s);
            m.setValue(value);
            String heartbeatMessage = gson.toJson(m);

            LOGGER.info("SocketConfig.socket Info: " + socketConfig.socket().getLocalPort());
            buf = heartbeatMessage.getBytes(StandardCharsets.UTF_8);
            InetAddress address = InetAddress.getByName("node1");
            DatagramPacket new_packet = new DatagramPacket(buf, buf.length, address, 5555);
            socket.send(new_packet);
//                count++;
//            }*/
        } catch (Exception e) {
            LOGGER.severe("main.Exception: " + e.toString());
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
