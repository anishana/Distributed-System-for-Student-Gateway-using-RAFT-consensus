package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.Entry;
import com.ds.management.models.Message;
import com.ds.management.models.NodeState;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;


@Component
public class UDPSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private DatagramSocket socket;
    private NodeState nodeState;

    @Autowired
    private SocketConfig socketConfig;

    @Value("${is.Master}")
    private String isMaster;

    @Value("${node.value}")
    private String nodeVal;

    @Autowired
    RequestUtilService requestUtilService;

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
        nodeState = NodeState.getNodeState();
        socket.setSoTimeout(300 );
        LOGGER.info("socket Info: " + socket.getLocalPort());
        LOGGER.info("Node Details: " + nodeState.toString());
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
                        Gson gson= new Gson();
                        Message packet= gson.fromJson(receivedMessage, Message.class);
                        parseMessage(packet, packet_received);
                    }
                } catch (SocketTimeoutException ex) {
                    getReadyForElection();
                }
            } catch (Exception ex) {
                LOGGER.info("\nException INSIDE LISTEN MESSAGE: \n" + receivedMessage + "\n ", ex);
            }
        }
        socket.close();
    }

    public void parseMessage(Message message, DatagramPacket receivedPacket) {
        try {
            String type= message.getRequest();
            if (type.equalsIgnoreCase(NodeConstants.REQUEST.APPEND_ENTRY.toString())) {
                LOGGER.info("Heartbeat received at: "+message);
                checkForHeartbeat(message);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.VOTE_REQUEST.toString())) {
                LOGGER.info("Got a Vote Request.");
                voteRequested(message, receivedPacket);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.VOTE_ACK.toString())) {
                LOGGER.info("Got a Vote Response.");
                updateVoteResponse(message);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.toString())) {
                LOGGER.info("Acknowledge Leader.");
                setLeader(message);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.LEADER_INFO.toString())) {
                LOGGER.info("Got Leader Info request.");
                requestUtilService.createLeaderInfo();
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN.toString())) {
                LOGGER.info("Got shutdown request.");
                shutDownNode();
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString())) {
                shutdownPropagate(Integer.parseInt(message.getSender_name()));
            }
            else if(type.equalsIgnoreCase(NodeConstants.REQUEST.CONVERT_FOLLOWER.toString())){
                convertFollower();
            }
            else if(type.equalsIgnoreCase(NodeConstants.REQUEST.TIMEOUT.toString())){
                setTimeout();
            }else if(type.equalsIgnoreCase(NodeConstants.REQUEST.STORE.toString())){
                LOGGER.info("Store Message.");
                storeRequest(message);
            }else if(type.equalsIgnoreCase(NodeConstants.REQUEST.RETRIEVE.toString())){
                LOGGER.info("Retrive Message.");
                retrieveMessage(message);
            }
        } catch (Exception e) {
            LOGGER.error("parseMessage-------Exception: \n"+ message+ "\n"+ e);
        }

    }


    public void getReadyForElection() {
        byte[] buf;
        try {
            if (nodeState.getServer_state() == NodeConstants.SERVER_STATE.FOLLOWER) {
                requestUtilService.updateNodeState();
                buf = requestUtilService.createRequestVoteRPCObject().getBytes(StandardCharsets.UTF_8);
                requestUtilService.sendPacketToAll(buf);
            }
        } catch (Exception ex) {
            LOGGER.info("EXCEPTION RAISED WHILE ELECTION: " + ex.getMessage());
        }
    }

    public void voteRequested(Message voteRequestMessage, DatagramPacket receivedPacket) {
        Gson gson= new Gson();
        Message responseMessage= requestUtilService.createVoteResponse(voteRequestMessage);
        byte[] buf= gson.toJson(responseMessage).getBytes(StandardCharsets.UTF_8);

        InetAddress from_address = receivedPacket.getAddress();
        int from_port = receivedPacket.getPort();
        DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, from_address, from_port);
        try {
            socket.send(packetToSend);
        } catch (Exception exception) {
            LOGGER.info("EXCEPTION WHILE SENDING VOTE RESPONSE; vote for:" + voteRequestMessage.getSender_name() + "; voted by: " + responseMessage.getSender_name() );
        }
    }

    public void updateVoteResponse(Message message) {
        LOGGER.info("Vote response: "+message.toString());
        int requestTerm = message.getTerm();
        String votedBy= message.getSender_name();
        int hasVoted= Integer.parseInt(message.getValue());
        if (requestTerm == nodeState.getTerm() && hasVoted==1) {
            int currentVotes = nodeState.getNumberOfVotes();
            nodeState.setNumberOfVotes(currentVotes + 1);
            nodeState.getVotedBy().add(votedBy);
            if (nodeState.getNumberOfVotes() >= NodeInfo.majorityNodes) {
                LOGGER.info("THE LEADER IS: " + nodeState.getNodeName()+ "; acknowledging other nodes.");
                nodeState.setIsLeader(true);
                acknowledgeLeader();
            }
        }
    }

    public void acknowledgeLeader() {
        LOGGER.info("Are you acknowledging leader: "+nodeState.toString());
        Message leaderMessage= requestUtilService.createAcknowledgeLeaderRequest();

        Gson gson= new Gson();
        String message = gson.toJson(leaderMessage);
        byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        requestUtilService.sendPacketToAll(buf);
    }

    public void setLeader(Message message) {
        int leader = Integer.parseInt(message.getSender_name());
        Integer term= message.getTerm();
        if (leader== nodeState.getNodeValue()) {
            nodeState.setIsLeader(true);
            nodeState.setServer_state(NodeConstants.SERVER_STATE.LEADER);
        } else {
            nodeState.setIsLeader(false);
            nodeState.setServer_state(NodeConstants.SERVER_STATE.FOLLOWER);
        }
        nodeState.setCurrentLeader(leader);
        nodeState.setTerm(term);
        LOGGER.info("Setting leader for term: " + nodeState.getTerm() + "; leader is : " + leader);
    }

    public void setTimeout(){
        try{
            getReadyForElection();
        } catch (Exception ex){
            LOGGER.info("Exception while setting timeout and starting election.");
        }
    }

    public void convertFollower(){
        try{
            nodeState.setServer_state(NodeConstants.SERVER_STATE.FOLLOWER);
            nodeState.setIsLeader(false);
        }catch (Exception exception){
            LOGGER.info("Exception");
        }
    }


    public void shutDownNode() {
        if (!socket.isClosed()) {
            LOGGER.info("Closing socket " + nodeVal);
            Message message= requestUtilService.createShutdownRequest();
            Gson gson= new Gson();
            byte[] buf = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
            requestUtilService.sendPacketToAll(buf);
            socket.close();
            isRunning = false;
        }
    }

    public void shutdownPropagate(Integer nodeId) {
        if (NodeInfo.totalNodes > 1) {
            String shutdownAddress = "";
            for (String removedAddress : NodeInfo.addresses) {
                if (removedAddress.equalsIgnoreCase(nodeState.getNodeName()))
                    shutdownAddress = nodeState.getNodeName();
            }
            NodeInfo.totalNodes--;
            NodeInfo.majorityNodes = (int) Math.ceil(NodeInfo.totalNodes / 2.0);
            NodeInfo.addresses.remove(shutdownAddress);
        }
        LOGGER.info("shutdownPropagate. NodeInfo.addresses: " + NodeInfo.addresses.toString());
    }

    public void storeRequest(Message receivedMessage){
        if(nodeState.getIsLeader()){
            Entry entry= requestUtilService.createEntryFromStoreRequest(receivedMessage);
            nodeState.getEntries().add(entry);
        } else {
            requestUtilService.createLeaderInfo();
        }
    }

    public void retrieveMessage(Message receivedMessage){
        if (nodeState.getIsLeader()){
            requestUtilService.createRetrieveMessage();
        } else {
            requestUtilService.createLeaderInfo();
        }
    }

    public void checkForHeartbeat(Message message){
       if(message.getLog()== null){
           LOGGER.info("Heartbeat received from: "+message.getSender_name());
       } else {
           LOGGER.info("AppendEntry Message.");
       }
    }

}