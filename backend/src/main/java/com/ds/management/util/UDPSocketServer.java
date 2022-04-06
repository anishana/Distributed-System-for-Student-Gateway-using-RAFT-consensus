package com.ds.management.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.*;

@Service
public class UDPSocketServer{

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketServer.class);

    private byte[] buffer;
    private InetAddress address;
    private DatagramSocket socket;

    public UDPSocketServer() throws SocketException {
        socket= new DatagramSocket();
    }

    @Scheduled(fixedRate = 1000)
    public void sendEcho() {
        try {
            String msg = "Hello World";
            System.out.println("S: " + msg);
            buffer = msg.getBytes();
            address= InetAddress.getByName("localhost");
            DatagramSocket socket= new DatagramSocket();
            DatagramPacket packet_exchange = new DatagramPacket(buffer, buffer.length, address, 6060);
            socket.send(packet_exchange);
            //packet_exchange = new DatagramPacket(buffer, buffer.length);
            //udpSocket.receive(packet_exchange);
            //String received = new String(packet_exchange.getData(), 0, packet_exchange.getLength());
            //return received;
        }
        catch (Exception ex){
            LOGGER.info("Exception caused: ", ex);
        }
    }

    public void close() throws SocketException {
        socket.close();
    }

}