package com.ds.Controller_Java.service.impl;

import com.ds.Controller_Java.config.SocketConfig;
import com.ds.Controller_Java.constants.NodeConstants;
import com.ds.Controller_Java.model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static com.ds.Controller_Java.constants.NodeConstants.clusterPort;
import static com.ds.Controller_Java.constants.NodeConstants.leaderName;

@Service
public class RequestServiceImpl implements RequestService {

    private DatagramSocket socket;

    @Autowired
    private SocketConfig socketConfig;

    private final static Logger LOGGER = Logger.getLogger(RequestServiceImpl.class.getName());

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
    }

    @Override
    public void getLeaderInfo() throws Exception {
        LOGGER.info("leader: " + leaderName.toString());
        byte[] buf = new byte[10240];
        Gson gson = new Gson();
        Message m = new Message();
        m.setSender_name("Controller");
        m.setKey("K1");
        m.setTerm(1);
        m.setRequest(NodeConstants.REQUEST.LEADER_INFO.toString());
        String message = gson.toJson(m);
        LOGGER.info("getLeaderInfo.message: " + message);
        buf = message.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName("node1");
        DatagramPacket new_packet = new DatagramPacket(buf, buf.length, address, clusterPort);
        socket.send(new_packet);
    }

    @Override
    public void sendStoreRequest(Message request) throws Exception {
        byte[] buf = new byte[10240];
        request.setRequest(NodeConstants.REQUEST.STORE.toString());
        Gson gson = new Gson();
        String message = gson.toJson(request);
        LOGGER.info("sendStoreRequest.message: " + message);
        buf = message.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(leaderName.toString().equalsIgnoreCase("") ? "node1" : leaderName.toString());
        DatagramPacket new_packet = new DatagramPacket(buf, buf.length, address, clusterPort);
        socket.send(new_packet);
    }

    @Override
    public void sendShutDownMessage(Message request) throws Exception {
        byte[] buf = new byte[10240];
        Gson gson = new Gson();
        request.setRequest(NodeConstants.REQUEST.SHUTDOWN.toString());
        String message = gson.toJson(request);
        LOGGER.info("sendShutdownMessage.message: " + message);
        buf = message.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(request.getReceiver());
        DatagramPacket new_packet = new DatagramPacket(buf, buf.length, address, clusterPort);
        socket.send(new_packet);
    }

    @Override
    public void sendMessage(Message request) throws Exception{
        byte[] buf = new byte[10240];
        Gson gson = new Gson();
        String message = gson.toJson(request);
        LOGGER.info("sendMessage.message: " + message);
        buf = message.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(request.getReceiver());
        DatagramPacket new_packet = new DatagramPacket(buf, buf.length, address, clusterPort);
        socket.send(new_packet);
    }

}
