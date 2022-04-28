package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.AppendEntryRPC;
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
            if (nodeState.getIsLeader() && !socket.isClosed()) {
                String message_to_send = createHeartbeatMessage();
                buf = message_to_send.getBytes(StandardCharsets.UTF_8);
                for (String add : NodeInfo.addresses) {
                    if(add.equalsIgnoreCase(nodeState.getNodeName()))
                        continue;
                    address = InetAddress.getByName(add);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                    socket.send(packet);
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Exception caused in send echo: ", ex);
        }
    }

    public String createHeartbeatMessage() {
        AppendEntryRPC ae= new AppendEntryRPC();
        ae.setTerm(nodeState.getTerm());
        ae.setLeaderId(nodeState.getNodeValue());
        ae.setType(NodeConstants.REQUEST.HEARTBEAT.ordinal());
        JSONObject jsonObject = new JSONObject(ae);
        String heartbeatMessage= jsonObject.toString();
        LOGGER.info("Sending heartbeat: " + heartbeatMessage);
        return heartbeatMessage;
    }

}