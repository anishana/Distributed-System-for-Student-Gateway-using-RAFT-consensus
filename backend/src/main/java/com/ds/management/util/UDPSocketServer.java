package com.ds.management.util;
import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.NodeState;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Service
@EnableAsync
public class UDPSocketServer{

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketServer.class);

    private byte[] buf;
    private InetAddress address;
    private DatagramSocket socket;
    private NodeState nodeState;

    @Value("${is.Master}")
    private String isMaster;

    @Value("${node.value}")
    private String val;

    @Autowired
    private SocketConfig socketConfig;

    public UDPSocketServer() throws UnknownHostException {
        buf= new byte[1024];
        address= InetAddress.getByName("localhost");
    }

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket= socketConfig.socket();
        nodeState= NodeState.getNodeState();
        nodeState.setNodeValue(val);
        LOGGER.info("SERVER: "+ NodeState.getNodeState());
        LOGGER.info("UDPSocketServer.socket Info: "+socket.getLocalPort());
    }

    @Async
    @Scheduled(fixedRate = 30000)
    public void sendEcho() {
        try {
            if(nodeState.getIsLeader() == true){
                String message_to_send= createHeartbeatMessage();
                buf= message_to_send.getBytes(StandardCharsets.UTF_8);
                for(String add: NodeInfo.addresses){
                    address= InetAddress.getByName(add);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 6060);
                    //LOGGER.info("SERVER: BEFORE: " + message_to_send);
                    socket.send(packet);
                    //LOGGER.info("SERVER: AFTER", packet.getData());
                }
                /*
                TO BE ADDED WHILE IMPLEMENTING APPEND RPC
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                LOGGER.info("SERVER: RECEIVED: ", received);
                */
            }
       }
        catch (Exception ex){
            LOGGER.info("Exception caused: ", ex);
        }
    }

    public String createHeartbeatMessage(){
        /*
        AppendEntryRPC heartbeat= new AppendEntryRPC();
        heartbeat.setLeaderId(NodeState.getNodeState().getNodeValue());
        heartbeat.setType(NodeConstants.REQUEST.HEARTBEAT);
        heartbeat.setTerm(nodeState.getTerm());
        Gson gson= new Gson();
        String message= gson.toJson(heartbeat);
        return message;
        */
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("leaderId", nodeState.getNodeValue());
        jsonObject.put("type", NodeConstants.REQUEST.HEARTBEAT.ordinal());
        jsonObject.put("term", nodeState.getTerm());
        String message= jsonObject.toString();
        return message;
    }

    public void close() throws SocketException {
        socket.close();
    }

}