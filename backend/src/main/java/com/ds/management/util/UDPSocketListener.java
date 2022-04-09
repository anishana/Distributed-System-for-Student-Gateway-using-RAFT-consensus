package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;


@Component
public class UDPSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private byte[] buf = new byte[102400];
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
        socket.setSoTimeout(nodeState.getTimeout());
//        socket.setSoTimeout(15000);
        LOGGER.info("socket Info: " + socket.getLocalPort());

        LOGGER.info("Node Details: " + nodeState.toString());
    }

    public void listen() {
        isRunning = true;
        String receivedMessage = "";
        while (isRunning) {
            try {
                DatagramPacket packet_received = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet_received);
                    if (buf != null && buf.length > 0) {
                        LOGGER.info("Length: " + packet_received.getLength());
                        receivedMessage = new String(packet_received.getData(), 0, packet_received.getLength());
                        parseMessage(receivedMessage, packet_received);
                    }
                } catch (SocketTimeoutException ex) {
                    if (!nodeState.getIsLeader())
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
            if (!message.equalsIgnoreCase("")) {

//                JSONObject jsonObject = new JSONObject(message);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

                int type = jsonObject.has("type") ? Integer.parseInt(jsonObject.get("type").getAsString()) : -1;
                String request = jsonObject.has("request") ? jsonObject.get("request").getAsString() : "";
                if (!request.equalsIgnoreCase("")) {

                    NodeConstants.REQUEST[] allRequests = NodeConstants.REQUEST.values();
                    for (NodeConstants.REQUEST d : allRequests) {
                        if (d.name().equals(request)) {
                            type = d.ordinal();
                            break;
                        }
                    }
                    LOGGER.info("type:" + type);
                }
                if (type == NodeConstants.REQUEST.HEARTBEAT.ordinal()) {
                    LOGGER.info("Heartbeat received at: " + message);
                } else if (type == NodeConstants.REQUEST.VOTE_REQUEST.ordinal()) {
                    LOGGER.info("Got a Vote Request.");
                    voteRequested(message, receivedPacket);
                } else if (type == NodeConstants.REQUEST.VOTE_ACK.ordinal()) {
                    LOGGER.info("Got a Vote Response.");
                    updateVoteResponse(message);
                } else if (type == NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal()) {
                    LOGGER.info("Acknowledge Leader.");
                    setLeader(message);
                } else if (type == NodeConstants.REQUEST.LEADER_INFO.ordinal()) {
                    LOGGER.info("Got Leader Info request.");
                    sendLeaderInfo();
                } else if (type == NodeConstants.REQUEST.SHUTDOWN.ordinal()) {
                    LOGGER.info("Got shutdown request.");
                    shutDownNode();
                } else if (type == NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.ordinal()) {
                    LOGGER.info("Got shutdown propagate request.");
                    shutdownPropagate(jsonObject.has("nodeId") ? jsonObject.get("nodeId").getAsString() : "");
                } else if (type == NodeConstants.REQUEST.CONVERT_FOLLOWER.ordinal()) {
                    convertFollower();
                } else if (type == NodeConstants.REQUEST.TIMEOUT.ordinal()) {
                    setTimeout();
                }
            }

        } catch (Exception e) {
            LOGGER.error("parseMessage-------Exception: " + e.getMessage() + "\n message: " + message);
        }

    }

    public void getReadyForElection() {
        try {
            if (nodeState.getServer_state() == NodeConstants.SERVER_STATE.FOLLOWER) {
                updateNodeState();
                buf = createRequestVoteRPCObject().getBytes(StandardCharsets.UTF_8);
                DatagramPacket new_packet;
                for (String add : NodeInfo.addresses) {
                    if (add.equalsIgnoreCase(nodeState.getNodeName()))
                        continue;
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
        nodeState.setHasVotedInThisTerm(true);
        nodeState.setNumberOfVotes(1);
        nodeState.setVotedFor(nodeState.getNodeName());
        Set<String> voteList = new HashSet<>();
        voteList.add(nodeState.getNodeName());
        nodeState.setVotedBy(voteList);
    }

    public String createRequestVoteRPCObject() {
        RequestVoteRPC voteObject = new RequestVoteRPC();
        voteObject.setCandidateId(nodeState.getNodeName());
        voteObject.setCandidate(Integer.parseInt(nodeVal));
        voteObject.setTerm(nodeState.getTerm());
        voteObject.setType(NodeConstants.REQUEST.VOTE_REQUEST.ordinal());
//        JSONObject jsonObject = new JSONObject(voteObject);
//        String voteRequestMessage = jsonObject.toString();
        String message = new Gson().toJson(voteObject);
//        gson.toJson(voteRequestMessage);

        LOGGER.info("VOTE REQ OBJECT BY:" + nodeState.getNodeName() + "; message: " + message);
        return message;
    }

    public void voteRequested(String receivedMessage, DatagramPacket receivedPacket) {
//        JSONObject obj = new JSONObject(receivedMessage);
//        String candidateId = obj.get("candidateId").toString();
//        Integer request_term = obj.getInt("term");

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(receivedMessage, JsonObject.class);
//        String candidateId = jsonObject.has("candidateId") ? jsonObject.get("candidateId").getAsString() : "";
        Integer candidate=jsonObject.has("candidate") ? jsonObject.get("candidate").getAsInt() : 1;
        Integer request_term = jsonObject.has("request_term") ? jsonObject.get("request_term").getAsInt() : -1;

        ResponseVoteRPC response = new ResponseVoteRPC();
        response.setType(NodeConstants.REQUEST.VOTE_ACK.ordinal());
        response.setVotedBy(nodeState.getNodeName());
        response.setTerm(request_term);
        LOGGER.info("candidateId:"+candidate);
        if (request_term > nodeState.getTerm()) {
            nodeState.setTerm(request_term);
            nodeState.setHasVotedInThisTerm(true);
            response.setHasVoted(1);
//            response.setVotedFor(candidateId);
        } else {
            if (nodeState.getHasVotedInThisTerm()) {
                response.setHasVoted(1);
            } else {
                response.setHasVoted(1);
//                response.setVotedFor(candidateId);
            }
        }
        LOGGER.info("Vote requested by: " + candidate + "; Vote done by: " + nodeState.getNodeName() + "; Has it voted? " + response.getHasVoted());
        if (response.getHasVoted() == 1) {
//            JSONObject voteResponse = new JSONObject(response);
            String message = new Gson().toJson(response);
            LOGGER.info("voteRequested.message:"+message);
            buf = message.getBytes(StandardCharsets.UTF_8);
            InetAddress from_address = receivedPacket.getAddress();
            int from_port = receivedPacket.getPort();
            DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, from_address, from_port);
            try {
                socket.send(packetToSend);
            } catch (Exception exception) {
                LOGGER.info("EXCEPTION WHILE SENDING VOTE RESPONSE; vote for:" + candidate + "; voted by: " + response.getVotedBy());
            }
        }
    }

    public void updateVoteResponse(String message) {

//        JSONObject object = new JSONObject(message);
        JsonObject obj =new Gson().fromJson(message,JsonObject.class);

        int requestTerm = Integer.parseInt(obj.get("term").getAsString());
        String votedBy = obj.get("votedBy").getAsString();


        LOGGER.info("Vote request in term: " + requestTerm + "; Vote done by: " + nodeState.getNodeName());
        if ((requestTerm == nodeState.getTerm()) && !(nodeState.getVotedBy().contains(votedBy))) {
            int currentVotes = nodeState.getNumberOfVotes();
            nodeState.setNumberOfVotes(currentVotes + 1);
            nodeState.getVotedBy().add(votedBy);
            if (nodeState.getNumberOfVotes() >= NodeInfo.majorityNodes) {
                LOGGER.info("THE LEADER IS: " + nodeState.getNodeName() + "; acknowledging other nodes.");
                acknowledgeLeader();
            }
        }
    }

    public void acknowledgeLeader() {
//        JSONObject obj = new JSONObject();
//        obj.put("type", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal());
//        obj.put("term", nodeState.getTerm());
//        obj.put("leaderId", nodeState.getNodeName());


        AcknowledgeLeader ack = new AcknowledgeLeader();
        ack.setType(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal());
        ack.setLeaderId(nodeState.getNodeName());
        ack.setTerm(nodeState.getTerm());

        String message = new Gson().toJson(ack);
        buf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket new_packet;
        try {
            for (String add : NodeInfo.addresses) {
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        } catch (Exception exception) {
            LOGGER.info("EXCEPTION!! ACKNOWLEDGE THE WINNER!!!!");
        }
    }

    public void setLeader(String message) {
//        JSONObject object = new JSONObject(message);
        JsonObject object = new Gson().fromJson(message,JsonObject.class);

        String leader = object.get("leaderId").getAsString();
        Integer term = Integer.parseInt(object.get("term").getAsString());
        LOGGER.info("leaderId: "+leader);
        LOGGER.info("nodeState.getNodeName(): "+nodeState.getNodeName());

        if (leader.equalsIgnoreCase(nodeState.getNodeName())) {
            nodeState.setIsLeader(true);
        } else {
            nodeState.setIsLeader(false);
        }
        nodeState.setCurrentLeader(leader);
        nodeState.setTerm(term);
        LOGGER.info("Setting leader for term: " + nodeState.getTerm() + "; leader is : " + leader);
    }

    public void sendLeaderInfo() {
//        JSONObject obj = new JSONObject();

        LeaderInfo l = new LeaderInfo();
        l.setType(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER_INFO.ordinal());
        l.setKey(NodeConstants.LEADER_KEY);
        l.setValue(nodeState.getNodeName());
        String parsed = new Gson().toJson(l);

        LOGGER.info(parsed);
    }

    public void shutDownNode() {
        if (!socket.isClosed()) {
            LOGGER.info("Closing socket " + nodeVal);
//            JSONObject obj = new JSONObject();
//            obj.put("type", NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.ordinal());
//            obj.put("request", NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.name());
//            obj.put("nodeId", nodeVal);

            ShutdownRpc sh = new ShutdownRpc();
            sh.setNodeId(nodeVal);
            sh.setRequest(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.name());
            sh.setType(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.ordinal());

            String message = new Gson().toJson(sh);
            buf = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket new_packet;
            try {
                for (String add : NodeInfo.addresses) {
                    InetAddress address = InetAddress.getByName(add);
                    new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                    socket.send(new_packet);
                }
            } catch (Exception exception) {
                LOGGER.info("EXCEPTION!! ACKNOWLEDGE THE WINNER!!!!");
            }
            socket.close();
            isRunning = false;
        }
    }


    public void shutdownPropagate(String nodeId) {
        if (NodeInfo.totalNodes > 1) {
            String shutdownAddress = "";
            for (String removedAddress : NodeInfo.addresses) {
                if (removedAddress.equalsIgnoreCase(nodeId))
                    shutdownAddress = nodeState.getNodeName();
            }
            NodeInfo.totalNodes--;
            NodeInfo.majorityNodes = (int) Math.ceil(NodeInfo.totalNodes / 2.0);
            NodeInfo.addresses.remove(shutdownAddress);
        }
        LOGGER.info("shutdownPropagate. NodeInfo.addresses: " + NodeInfo.addresses.toString());
    }

    public void convertFollower() {
        try {
            nodeState.setServer_state(NodeConstants.SERVER_STATE.FOLLOWER);
            nodeState.setIsLeader(false);
        } catch (Exception exception) {
            LOGGER.info("Exception");
        }
    }

    public void setTimeout() {
        try {
            getReadyForElection();
        } catch (Exception ex) {
            LOGGER.info("Exception while setting timeout and starting election.");
        }
    }
}