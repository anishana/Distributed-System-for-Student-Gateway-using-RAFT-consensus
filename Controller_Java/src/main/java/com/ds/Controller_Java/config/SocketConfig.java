package com.ds.Controller_Java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

@Configuration
public class SocketConfig {
    private final static Logger LOGGER = Logger.getLogger(SocketConfig.class.getName());

    @Bean
    public DatagramSocket socket() throws SocketException {
        DatagramSocket socket= new DatagramSocket(5555);
        LOGGER.info("SocketConfig.socket Info: "+socket.getLocalPort());
        return socket;
    }
}