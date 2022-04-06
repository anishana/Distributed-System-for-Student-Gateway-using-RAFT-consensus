package com.ds.management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.*;
import java.net.InetAddress;

public class UDPSocketListener implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private byte[] buffer = new byte[256];
    private DatagramSocket socket;

    public UDPSocketListener() throws SocketException {
        socket= new DatagramSocket(6060);
    }

    public void run() {
        isRunning = true;
        try {
            LOGGER.info("Inside RUN");
            while (isRunning) {
                LOGGER.info("IN RUN 2");
                int len = buffer.length;
                DatagramPacket packet_exchange = new DatagramPacket(buffer, len);
                socket.receive(packet_exchange);

                int port = packet_exchange.getPort();
                InetAddress inetAddress = packet_exchange.getAddress();
                packet_exchange = new DatagramPacket(buffer, len, inetAddress, port);
                String received = new String(packet_exchange.getData(), 0, packet_exchange.getLength());
                LOGGER.info("Received: "+ received);

                if (received.equals("end")) {
                    isRunning = false;
                    continue;
                }
                socket.send(packet_exchange);
            }
            socket.close();
        }
        catch (Exception ex){
            LOGGER.info("Exception: ", ex);
        }
    }
}
