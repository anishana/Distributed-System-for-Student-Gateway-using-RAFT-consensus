package com.ds.management.util;
import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.NodeState;
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

@Service
@EnableAsync
public class UDPSocketServer{

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketServer.class);

    private byte[] buf;
    private InetAddress address;
    private DatagramSocket socket;

    @Value("${is.Master}")
    private String isMaster;

    @Value("${node.value}")
    private Integer val;

    @Autowired
    private SocketConfig socketConfig;

    public UDPSocketServer() throws SocketException, UnknownHostException {
        buf= new byte[256];
        address= InetAddress.getByName("localhost");
    }

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket= socketConfig.socket();
//        socket= new DatagramSocket();
        NodeState.getNodeState().setNodeValue(val);
        LOGGER.info("SERVER: "+ NodeState.getNodeState());
        LOGGER.info("UDPSocketServer.socket Info: "+socket.getLocalPort());
    }

    @Async
    @Scheduled(fixedRate = 150)
    public void sendEcho() {
        try {
//                LOGGER.info("Sending");
//            if(isMaster.equalsIgnoreCase("yes")){
                buf = ".........sample........".getBytes();
                for(String add: NodeInfo.addresses){
                    address= InetAddress.getByName(add);
                    //LOGGER.info("PRINTING ADDRESS: "+address.toString());
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                    //LOGGER.info("SERVER: BEFORE: " + packet.getData());
                    socket.send(packet);
                    //LOGGER.info("SERVER: AFTER", packet.getData());
                }
                /*DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                //LOGGER.info("SERVER: RECEIVED: ", received);*/
//            }
       }
        catch (Exception ex){
            LOGGER.info("Exception caused: ", ex);
        }
    }


    public void close() throws SocketException {
        socket.close();
    }

}