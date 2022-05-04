package com.ds.Controller_Java.util;

import com.ds.Controller_Java.config.SocketConfig;
import com.ds.Controller_Java.constants.NodeConstants;
import com.ds.Controller_Java.model.Message;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static com.ds.Controller_Java.constants.NodeConstants.leaderName;

@Service
public class ControllerUdpListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ControllerUdpListener.class);

    private boolean isRunning;
    private DatagramSocket socket;

    @Autowired
    private SocketConfig socketConfig;

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
        LOGGER.info("socket Info: " + socket.getLocalPort());
    }


    public void listen() {
        byte[] buf = new byte[1024];
        isRunning = true;
        String receivedMessage = "";
        while (isRunning) {
            try {
                DatagramPacket packet_received = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet_received);
                    if (buf != null && buf.length > 0) {
                        receivedMessage = new String(packet_received.getData(), 0, packet_received.getLength());
                        Gson gson = new Gson();
                        LOGGER.info("message Received: "+receivedMessage);
                        Message packet = gson.fromJson(receivedMessage, Message.class);
                        parseMessage(packet, packet_received);
                    }
                } catch (SocketTimeoutException ex) {
                    LOGGER.error("listen.Error: ", ex);
                }
            } catch (Exception ex) {
                LOGGER.info("\nException INSIDE LISTEN MESSAGE: \n" + receivedMessage + "\n ", ex);
            }
        }
        socket.close();
    }

    public void parseMessage(Message message, DatagramPacket packet_received) {
        String type = message.getRequest();
        Gson gson = new Gson();
        if(type.equalsIgnoreCase(NodeConstants.REQUEST.LEADER_INFO.toString())){
            setLeader(message);
        } else if(type.equalsIgnoreCase(NodeConstants.REQUEST.RETRIEVE.toString())){
            LOGGER.info(gson.toJson(message));
        }

    }

    private void setLeader(Message message){
        leaderName.delete(0, leaderName.length());
        leaderName.append(message.getValue());
        LOGGER.info("leaderName: "+leaderName);
    }

}
