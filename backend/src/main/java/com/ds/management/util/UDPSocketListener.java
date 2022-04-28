package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.Message;
import com.ds.management.models.NodeState;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


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

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
        nodeState = NodeState.getNodeState();
        socket.setSoTimeout(5000);
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
                        parseMessage(receivedMessage, packet_received);
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

    public void parseMessage(String message, DatagramPacket receivedPacket) {
        try {
            LOGGER.info("parseMessage.message: " + message);
            Gson gson= new Gson();
            Message packet= gson.fromJson(message, Message.class);
            String type= packet.getRequest();
            if (type.equalsIgnoreCase(NodeConstants.REQUEST.HEARTBEAT.toString())) {
                LOGGER.info("Heartbeat received at: "+message);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.VOTE_REQUEST.toString())) {
                LOGGER.info("Got a Vote Request.");
                voteRequested(packet, receivedPacket);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.VOTE_ACK.toString())) {
                LOGGER.info("Got a Vote Response.");
                updateVoteResponse(packet);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.toString())) {
                LOGGER.info("Acknowledge Leader.");
                setLeader(packet);
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.LEADER_INFO.toString())) {
                LOGGER.info("Got Leader Info request.");
                sendLeaderInfo();
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN.toString())) {
                LOGGER.info("Got shutdown request.");
                shutDownNode();
            } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString())) {
                shutdownPropagate(Integer.parseInt(packet.getSender_name()));
            }
            else if(type.equalsIgnoreCase(NodeConstants.REQUEST.CONVERT_FOLLOWER.toString())){
                convertFollower();
            }
            else if(type.equalsIgnoreCase(NodeConstants.REQUEST.TIMEOUT.toString())){
                setTimeout();
            }

        } catch (Exception e) {
            LOGGER.error("parseMessage-------Exception: \n"+ message+ "\n"+ e);
        }

    }


    public void getReadyForElection() {
        byte[] buf;
        try {
            if (nodeState.getServer_state() == NodeConstants.SERVER_STATE.FOLLOWER) {
                updateNodeState();
                buf = createRequestVoteRPCObject().getBytes(StandardCharsets.UTF_8);
                DatagramPacket new_packet;
                for (String add : NodeInfo.addresses) {
                    InetAddress address = InetAddress.getByName(add);
                    new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                    socket.send(new_packet);
                }
            }
        } catch (Exception ex) {
            LOGGER.info("EXCEPTION RAISED WHILE ELECTION: " + ex.getMessage());
        }
    }

    public void updateNodeState() {
        int new_term = nodeState.getTerm() + 1;
        nodeState.setTerm(new_term);
        nodeState.setServer_state(NodeConstants.SERVER_STATE.CANDIDATE);
        nodeState.setNumberOfVotes(0);
        nodeState.setHasVotedInThisTerm(false);
    }

    public String createRequestVoteRPCObject() {
        Message message= new Message();
        message.setRequest(NodeConstants.REQUEST.VOTE_REQUEST.toString());
        message.setTerm(nodeState.getTerm());
        message.setSender_name(nodeState.getNodeValue().toString());
        Gson gson= new Gson();
        String voteRequestMessage= gson.toJson(message);
        LOGGER.info("VOTE REQ OBJECT BY:"+nodeState.getNodeName()+ "; message: " + voteRequestMessage);
        return voteRequestMessage;
    }

    public void voteRequested(Message voteRequestMessage, DatagramPacket receivedPacket) {
        byte[] buf = new byte[1024];
        Integer requestTerm = voteRequestMessage.getTerm();
        Boolean hasVoted= false;

        Message responseMessage= new Message();
        responseMessage.setRequest(NodeConstants.REQUEST.VOTE_ACK.toString());
        responseMessage.setSender_name(nodeState.getNodeValue().toString());
        responseMessage.setTerm(requestTerm);

        if (requestTerm > nodeState.getTerm()) {
            nodeState.setTerm(requestTerm);
            nodeState.setHasVotedInThisTerm(true);
            hasVoted= true;
        } else {
            if (!nodeState.getHasVotedInThisTerm()) {
                hasVoted= true;
                nodeState.setHasVotedInThisTerm(true);
            }
        }
        LOGGER.info("Vote requested by: "+voteRequestMessage.getSender_name()+ "; Vote done by: "+nodeState.getNodeValue()+"; Has it voted? "+hasVoted);
        int value= 0;
        if(hasVoted){
            value=1;
        }
        responseMessage.setValue(String.valueOf(value));

        Gson gson= new Gson();
        buf= gson.toJson(responseMessage).getBytes(StandardCharsets.UTF_8);
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
        Message leaderMessage= new Message();
        leaderMessage.setSender_name(nodeState.getNodeValue().toString());
        leaderMessage.setTerm(nodeState.getTerm());
        leaderMessage.setRequest(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.toString());

        Gson gson= new Gson();
        String message = gson.toJson(leaderMessage);
        byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket new_packet;
        try {
            for (String add : NodeInfo.addresses) {
                if(add.equalsIgnoreCase(nodeState.getNodeName()))
                    continue;
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        } catch (Exception exception) {
            LOGGER.info("EXCEPTION!! ACKNOWLEDGE THE WINNER!!!!");
        }
    }

    public void setLeader(Message message) {
        int leader = Integer.parseInt(message.getSender_name());
        Integer term= message.getTerm();
        if (leader== nodeState.getNodeValue()) {
            nodeState.setIsLeader(true);
        } else {
            nodeState.setIsLeader(false);
        }
        nodeState.setCurrentLeader(leader);
        nodeState.setTerm(term);
        LOGGER.info("Setting leader for term: " + nodeState.getTerm() + "; leader is : " + leader);
    }

    public void sendLeaderInfo() {
        Gson gson= new Gson();
        Map<String, String> map= new HashMap<String, String>();
        map.put("type", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER_INFO.toString());
        map.put("key", NodeConstants.LEADER_KEY);
        map.put("value", nodeState.getNodeName());
        LOGGER.info(gson.toJson(map));
    }

    public void setTimeout(){
        try{
            getReadyForElection();
        }
        catch (Exception ex){
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
            Message message= new Message();
            message.setSender_name(nodeVal);
            message.setRequest(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString());
            Gson gson= new Gson();
            byte[] buf = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
            DatagramPacket new_packet;
            try {
                for (String add : NodeInfo.addresses) {
                    InetAddress address = InetAddress.getByName(add);
                    new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                    socket.send(new_packet);
                }
            } catch (Exception exception) {
                LOGGER.info("EXCEPTION in shutdown!!!!");
            }
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

}