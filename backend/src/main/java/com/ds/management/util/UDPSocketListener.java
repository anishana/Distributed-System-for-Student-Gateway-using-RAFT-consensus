package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.NodeState;
import com.ds.management.models.RequestVoteRPC;
import com.ds.management.models.ResponseVoteRPC;
import org.json.JSONObject;
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
import java.util.HashSet;
import java.util.regex.Matcher;


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
                        receivedMessage = new String(packet_received.getData(), 0, packet_received.getLength());
                        parseMessage(receivedMessage, packet_received);
                    }
                } catch (SocketTimeoutException ex) {
                    LOGGER.error("listen.Exception: ", ex);
                    getReadyForElection();
                }

//                LOGGER.info("Message received: "+receivedMessage);
//                InetAddress from_address = packet_received.getAddress();
//                int from_port = packet_received.getPort();
//                DatagramPacket packetToBeSent = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
//                socket.send(packet);
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
                JSONObject jsonObject = new JSONObject(message);
                int type = jsonObject.has("type") ? Integer.parseInt(jsonObject.get("type").toString()) : -1;
                String request = jsonObject.has("request") ? jsonObject.get("request").toString() : "";
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
                    LOGGER.info("Got Heartbeat.");
                } else if (type == NodeConstants.REQUEST.VOTE_REQUEST.ordinal()) {
                    LOGGER.info("Got a Vote Request.");
                    voteRequested(message, receivedPacket);
                } else if (type == NodeConstants.REQUEST.VOTE_RESPONSE.ordinal()) {
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
                    shutdownPropagate(jsonObject.has("nodeId") ? jsonObject.getInt("nodeId") : -1);
                }
            }

        } catch (Exception e) {
//            LOGGER.error("message: " + message);
            LOGGER.error("parseMessage.Exception: ", e);
        }

    }

    public void getReadyForElection() {
        try {
            if (nodeState.getServer_state() == NodeConstants.SERVER_STATE.FOLLOWER) {
                updateNodeState();
                String requestVoteRPC = createRequestVoteRPCObject();
                buf = requestVoteRPC.getBytes(StandardCharsets.UTF_8);
                DatagramPacket new_packet;
                for (String add : NodeInfo.addresses) {
                    InetAddress address = InetAddress.getByName(add);
                    LOGGER.info("VOTE REQ OBJECT: address: " + address.toString() + "; message: " + requestVoteRPC);
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
        nodeState.setHasVotedInThisTerm(false);
        nodeState.setNumberOfVotes(0);
        nodeState.setVotedFor(-1);
        nodeState.setVotedBy(new HashSet<>());
    }

    public String createRequestVoteRPCObject() {
        RequestVoteRPC voteObject = new RequestVoteRPC();
        voteObject.setCandidateId(NodeState.getNodeState().getNodeValue());
        voteObject.setTerm(nodeState.getTerm());
        voteObject.setType(NodeConstants.REQUEST.VOTE_REQUEST.ordinal());
        JSONObject jsonObject = new JSONObject(voteObject);
        String voteRequestMessage = jsonObject.toString();
        LOGGER.info("VOTE REQ OBJECT: " + voteRequestMessage);
        return voteRequestMessage;
    }

    public void voteRequested(String receivedMessage, DatagramPacket receivedPacket) {
        JSONObject obj = new JSONObject(receivedMessage);
        String candidateId = obj.get("candidateId").toString();
        Integer request_term = Integer.parseInt(obj.get("term").toString());

        ResponseVoteRPC response = new ResponseVoteRPC();
        response.setType(NodeConstants.REQUEST.VOTE_RESPONSE.ordinal());
        response.setVotedBy(nodeState.getNodeValue());
        response.setTerm(request_term);

        if (request_term > nodeState.getTerm()) {
            nodeState.setTerm(request_term);
            nodeState.setHasVotedInThisTerm(true);
            response.setHasVoted(true);
            response.setVotedFor(candidateId);
        } else {
            if (nodeState.getHasVotedInThisTerm()) {
                response.setHasVoted(false);
            } else {
                response.setHasVoted(true);
                response.setVotedFor(candidateId);
            }
        }
        if (response.getHasVoted()) {
            JSONObject voteResponse = new JSONObject(response);
            buf = voteResponse.toString().getBytes(StandardCharsets.UTF_8);
            InetAddress from_address = receivedPacket.getAddress();
            int from_port = receivedPacket.getPort();
            DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, from_address, from_port);
            try {
                socket.send(packetToSend);
            } catch (Exception exception) {
                LOGGER.info("EXCEPTION WHILE SENDING VOTE RESPONSE; vote for:" + candidateId + "; voted by: " + response.getVotedBy());
            }
        }
    }

    public void updateVoteResponse(String message) {
        LOGGER.info("VOTE RESPONSE: " + message);
        JSONObject object = new JSONObject(message);
        int requestTerm = Integer.parseInt(object.get("term").toString());
        String votedBy = object.get("votedBy").toString();
        LOGGER.info("VOTED BY: " + votedBy + "; SET:" + nodeState.getVotedBy());
        if ((requestTerm == nodeState.getTerm()) && !(nodeState.getVotedBy().contains(votedBy))) {
            int currentVotes = nodeState.getNumberOfVotes();
            nodeState.setNumberOfVotes(currentVotes + 1);
            nodeState.getVotedBy().add(votedBy);
            if (nodeState.getNumberOfVotes() >= NodeInfo.majorityNodes) {
                LOGGER.info("THE WINNER IS: " + nodeState.getNodeValue());
                //TO DO-> write a function which shall
                //acknowledge the winner
                acknowledgeLeader();
            }
        }
    }

    public void acknowledgeLeader() {
        JSONObject obj = new JSONObject();
        obj.put("type", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal());
        obj.put("term", nodeState.getTerm());
        obj.put("leaderId", nodeState.getNodeValue());
        String message = obj.toString();
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
        JSONObject object = new JSONObject(message);
        String leader = object.get("leaderId").toString();
        Integer term = Integer.parseInt(object.get("term").toString());
        if (leader.equalsIgnoreCase(nodeState.getNodeValue())) {
            nodeState.setIsLeader(true);
        } else {
            nodeState.setIsLeader(false);
        }
        nodeState.setCurrentLeader(leader);
        nodeState.setTerm(term);
        LOGGER.info("Current node: " + nodeState.getNodeValue() + "; leader : " + nodeState.getCurrentLeader());
    }

    public void sendLeaderInfo() {
        JSONObject obj = new JSONObject();
        obj.put("type", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER_INFO.ordinal());
        obj.put("key", NodeConstants.LEADER_KEY);
        obj.put("value", nodeState.getCurrentLeader());
        obj.put("request", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER_INFO.name());

        String message = obj.toString();
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

    public void shutDownNode() {
        if (!socket.isClosed()) {
            LOGGER.info("Closing socket " + nodeVal);
            JSONObject obj = new JSONObject();
            obj.put("type", NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.ordinal());
            obj.put("request", NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.name());
            obj.put("nodeId", nodeVal);
            String message = obj.toString();
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


    public void shutdownPropagate(Integer nodeId) {
        if (NodeInfo.totalNodes > 1) {
            String shutdownAddress = "";
            for (String removedAddress : NodeInfo.addresses) {
                if (removedAddress.equalsIgnoreCase("Node" + nodeId))
                    shutdownAddress = "Node" + nodeId;
//                    NodeInfo.addresses.remove("Node"+nodeId);

            }
            NodeInfo.totalNodes--;
            NodeInfo.majorityNodes = (int) Math.ceil(NodeInfo.totalNodes / 2.0);
            NodeInfo.addresses.remove(shutdownAddress);
        }
        LOGGER.info("shutdownPropagate. NodeInfo.addresses: " + NodeInfo.addresses.toString());

        /*else {

        }*/
    }
}
