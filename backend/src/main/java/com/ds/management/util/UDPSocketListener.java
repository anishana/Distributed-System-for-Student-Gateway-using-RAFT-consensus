package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.Entry;
import com.ds.management.models.Message;
import com.ds.management.models.NodeState;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static com.ds.management.models.NodeState.getNodeState;


@Component
public class UDPSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean shutdown;
    private boolean isRunning;
    private DatagramSocket socket;
    private NodeState nodeState;

    @Autowired
    private SocketConfig socketConfig;

    @Value("${is.Master}")
    private String isMaster;

    @Value("${node.value}")
    private String nodeVal;


    @Value("${log.value}")
    private String logFileName;

    @Autowired
    RequestUtilService requestUtilService;

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket = socketConfig.socket();
        nodeState = getNodeState();
        socket.setSoTimeout(3000);
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
                        Gson gson = new Gson();
                        Message packet = gson.fromJson(receivedMessage, Message.class);
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
            String type = message.getRequest();
            String key = message.getRequest();

            if (!getNodeState().getCurrentShutdownState()) {
                if (type.equalsIgnoreCase(NodeConstants.REQUEST.APPEND_ENTRY.toString())) {
                    LOGGER.info("Heartbeat received at: " + message);
                    checkForHeartbeat(message, receivedPacket);
                    //Added if the election is done before the application is up. Edge case.
                    if (getNodeState().getCurrentLeader() == null) {
                        setLeader(message);
                    }
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
                    sendLeaderInfo(receivedPacket);
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN.toString())) {
                    LOGGER.info("Got shutdown request.");
                    shutDownNode();
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString())) {
                    shutdownPropagate(message.getSender_name());
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.CONVERT_FOLLOWER.toString())) {
                    convertFollower();
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.TIMEOUT.toString())) {
                    setTimeout();
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.STORE.toString())) {
                    LOGGER.info("Store Message.");
                    storeRequest(message, receivedPacket);
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.RETRIEVE.toString())
                        && !(key.equalsIgnoreCase(NodeConstants.REQUEST.COMMITTED_LOGS.toString()))) {
                    LOGGER.info("Retrieve Message Request.");
                    retrieveMessage(message, receivedPacket);
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.RETRIEVE.toString())
                        && key.equalsIgnoreCase(NodeConstants.REQUEST.COMMITTED_LOGS.toString())) {
                    LOGGER.info("Received Retrieve Message." + message);
                } else if (type.equalsIgnoreCase(NodeConstants.REQUEST.APPEND_REPLY.toString())) {
                    LOGGER.info("Received Append Reply." + message);
                    receiveAppendReply(message, receivedPacket);
                }
            } else {
                if (type.equalsIgnoreCase(NodeConstants.REQUEST.RESUME.toString()))
                    resumeNode();
            }
        } catch (Exception e) {
            LOGGER.error("parseMessage-------Exception: \n" + message + "\n" + e);
        }

    }


    public void getReadyForElection() {
        byte[] buf;
        try {
            if (nodeState.getServer_state() == NodeConstants.SERVER_STATE.FOLLOWER) {
                requestUtilService.updateNodeState();
                requestUtilService.sendPacketToAll(requestUtilService.createRequestVoteRPCObject());
            }
        } catch (Exception ex) {
            LOGGER.info("EXCEPTION RAISED WHILE ELECTION: " + ex.getMessage());
        }
    }

    public void voteRequested(Message voteRequestMessage, DatagramPacket receivedPacket) {
        Gson gson = new Gson();
        Message responseMessage = requestUtilService.createVoteResponse(voteRequestMessage);
        byte[] buf = gson.toJson(responseMessage).getBytes(StandardCharsets.UTF_8);

        InetAddress from_address = receivedPacket.getAddress();
        int from_port = receivedPacket.getPort();
        DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, from_address, from_port);
        try {
            socket.send(packetToSend);
        } catch (Exception exception) {
            LOGGER.info("EXCEPTION WHILE SENDING VOTE RESPONSE; vote for:" + voteRequestMessage.getSender_name() + "; voted by: " + responseMessage.getSender_name());
        }
    }

    public void updateVoteResponse(Message message) {
        LOGGER.info("Vote response: " + message.toString());
        int requestTerm = message.getTerm();
        String votedBy = message.getSender_name();
        int hasVoted = Integer.parseInt(message.getValue());
        if (requestTerm == nodeState.getTerm() && hasVoted == 1) {
            int currentVotes = nodeState.getNumberOfVotes();
            nodeState.setNumberOfVotes(currentVotes + 1);
//            nodeState.getVotedBy().add(votedBy);
            if (nodeState.getNumberOfVotes() >= NodeInfo.majorityNodes) {
                LOGGER.info("THE LEADER IS: " + nodeState.getNodeName() + "; acknowledging other nodes.");
                nodeState.setIsLeader(true);
                acknowledgeLeader();
            }
        }
    }

    public void acknowledgeLeader() {
        LOGGER.info("Are you acknowledging leader: " + nodeState.toString());
        Message leaderMessage = requestUtilService.createAcknowledgeLeaderRequest();

        Gson gson = new Gson();
        String message = gson.toJson(leaderMessage);
        requestUtilService.sendPacketToAll(message);
    }

    public void setLeader(Message message) {
//        int leader = Integer.parseInt(message.getSender_name());
        String leader = message.getSender_name();
        Integer term = message.getTerm();
        if (leader.equalsIgnoreCase(nodeState.getNodeName())) {
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

    public void setTimeout() {
        try {
            getReadyForElection();
        } catch (Exception ex) {
            LOGGER.info("Exception while setting timeout and starting election.");
        }
    }

    public void convertFollower() {
        try {
            nodeState.setServer_state(NodeConstants.SERVER_STATE.FOLLOWER);
            nodeState.setIsLeader(false);
        } catch (Exception exception) {
            LOGGER.info("Exception");
        }
    }


    public void shutDownNode() {
        if (!socket.isClosed()) {
            LOGGER.info("Closing socket " + nodeVal);
//            Message message = requestUtilService.createShutdownRequest();
//            Gson gson = new Gson();
//            requestUtilService.sendPacketToAll(gson.toJson(message));
            NodeState.getNodeState().setCurrentShutdownState(true);
//            shutdown = true;
//            isRunning = false;
//            socket.close();
        }
    }

    public void shutdownPropagate(String nodeId) {
        if (NodeInfo.totalNodes > 1) {
            String shutdownAddress = "";
            for (String removedAddress : NodeInfo.addresses) {
                if (removedAddress.equalsIgnoreCase(nodeId)) {
                    shutdownAddress = nodeState.getNodeName();
                    break;
                }
            }
            NodeInfo.totalNodes--;
            NodeInfo.majorityNodes = (int) Math.ceil(NodeInfo.totalNodes / 2.0);
            NodeInfo.addresses.remove(shutdownAddress);
        }
        LOGGER.info("shutdownPropagate. NodeInfo.addresses: " + NodeInfo.addresses.toString());
    }

    public void resumeNode() {
        getNodeState().setCurrentShutdownState(false);
    }

    public void storeRequest(Message receivedMessage, DatagramPacket receivedPacket) {
        if (nodeState.getIsLeader()) {
            LOGGER.info("storeRequest.receivedMessage: " + receivedMessage.toString());
            Entry entry = requestUtilService.createEntryFromStoreRequest(receivedMessage);
            LOGGER.info("storeRequest.entry: " + entry.toString());
            nodeState.getEntries().add(entry);
            nodeState.getCurrentAppendReplyCount().add(0);
        } else {
            sendLeaderInfo(receivedPacket);
        }
    }

    public void retrieveMessage(Message receivedMessage, DatagramPacket receivedPacket) {
        if (nodeState.getIsLeader()) {
            String message = requestUtilService.createRetrieveMessage();
            requestUtilService.sendPacketToNode(message, receivedPacket.getAddress(), receivedPacket.getPort());

        } else {
            sendLeaderInfo(receivedPacket);
        }
    }

    public void checkForHeartbeat(Message message, DatagramPacket receivedPacket) {

        if (message.getLog() == null || message.getLog().size() == 0) {
            if (message.getCommitIndex().intValue() != NodeState.getNodeState().getCommitIndex().intValue()) {
                NodeState.getNodeState().setCommitIndex(message.getCommitIndex());
            }
//            LOGGER.info("Heartbeat received from: " + message.getSender_name());
        } else {
            LOGGER.info("AppendEntry Message.");

            if (message.getCommitIndex().intValue() == NodeState.getNodeState().getLastApplied())
                sendAppendReply(true, receivedPacket);
            else
                sendAppendReply(false, receivedPacket);
        }
    }

    private void writeOnPersistentFile(String message) {
        try {
            File logFile = new File(logFileName);
            if (!logFile.exists())
                LOGGER.info("logFile.createNewFile()" + logFile.createNewFile());
            FileOutputStream outputStream = new FileOutputStream(logFileName, true);
            byte[] strToBytes = message.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            LOGGER.error("writeOnPersistentFile.Error: ", e);
        }

    }

    public void sendLeaderInfo(DatagramPacket receivedPacket) {
        String message = requestUtilService.createLeaderInfo();
        LOGGER.info("Leader Info: " + message);
        requestUtilService.sendPacketToNode(message, receivedPacket.getAddress(), receivedPacket.getPort());
    }

    public void sendAppendReply(boolean reply, DatagramPacket receivedPacket) {
        if (reply) {
            Integer lastApplied = getNodeState().getLastApplied();
            getNodeState().setLastApplied(++lastApplied);
        }
        String message = requestUtilService.createAppendReply(reply);
        requestUtilService.sendPacketToNode(message, receivedPacket.getAddress(), receivedPacket.getPort());
    }

    public void receiveAppendReply(Message message, DatagramPacket receivedPacket) {
        if (message.getSuccess()) {

            Integer nextIndex = NodeState.getNodeState().getNextIndex().get(message.getSender_name());
            NodeState.getNodeState().getNextIndex().put(message.getSender_name(), ++nextIndex);
            NodeState.getNodeState().getMatchIndex().put(message.getSender_name(), message.getMatchIndex());

            NodeState.getNodeState().incrementCurrentAppendReplyCount(message.getMatchIndex() - 1);
            checkAndIncrementCommitIndex(message.getMatchIndex() - 1);
        }
    }

    private void checkAndIncrementCommitIndex(Integer matchIndex) {
        LOGGER.info("matchIndex: " + matchIndex + ", commitIndex: " + NodeState.getNodeState().getCommitIndex() + ", currentAppendReplyCount: " + NodeState.getNodeState().getCurrentAppendReplyCount().get(matchIndex) + ", ");
        if ((matchIndex + 1) > NodeState.getNodeState().getCommitIndex() && NodeState.getNodeState().getCurrentAppendReplyCount().get(matchIndex) >= NodeInfo.majorityNodes) {
            NodeState.getNodeState().setCommitIndex(NodeState.getNodeState().getCommitIndex() + 1);
        }
    }

}