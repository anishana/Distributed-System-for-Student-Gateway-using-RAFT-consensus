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
import java.nio.charset.StandardCharsets;

@Service
@EnableAsync
public class UDPSocketServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketServer.class);

    private byte[] buf;
    private InetAddress address;
    private DatagramSocket socket;
    private NodeState nodeState;

    @Value("${is.Master}")
    private String isMaster;

    @Value("${node.value}")
    private Integer val;

    @Autowired
    private SocketConfig socketConfig;

    @Autowired
    RequestUtilService requestUtilService;

    public UDPSocketServer() throws UnknownHostException {
        buf = new byte[102400];
        address = InetAddress.getByName("localhost");
    }

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
        nodeState = NodeState.getNodeState();
        nodeState.setNodeValue(val);
        nodeState.setNodeName("Node"+val);
        LOGGER.info("SERVER: " + NodeState.getNodeState());
        LOGGER.info("UDPSocketServer.socket Info: " + socket.getLocalPort());
    }

    @Async
    @Scheduled(fixedRate = 150)
    public void sendEcho() {
        try {
            if (nodeState.getIsLeader() && !socket.isClosed() && !NodeState.getNodeState().getCurrentShutdownState()) {
                for (String add : NodeInfo.addresses) {
                    InetAddress address = InetAddress.getByName(add);
                    requestUtilService.sendPacketToNode(requestUtilService.createHeartBeatMessage(add), address, NodeInfo.port);
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Exception caused in send echo: ", ex);
        }
    }

}