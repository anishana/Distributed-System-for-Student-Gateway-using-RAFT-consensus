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
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.annotation.PostConstruct;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static com.ds.management.models.NodeState.getNodeState;

@Service
public class RequestUtilService {

    private DatagramSocket socket;
    @Value("${node.value}")
    private String nodeVal;


    @Autowired
    private SocketConfig socketConfig;

    @PostConstruct
    public void getSocketConfig() {
        try {
            socket = socketConfig.socket();
        } catch (SocketException e) {
            LOGGER.error("getSocketConfig.Error: ", e);
        }
    }

    private static NodeState nodeState = getNodeState();

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestUtilService.class);

    public void updateNodeState() {
        int new_term = nodeState.getTerm() + 1;
        nodeState.setTerm(new_term);
        nodeState.setServer_state(NodeConstants.SERVER_STATE.CANDIDATE);
        nodeState.setNumberOfVotes(0);
        nodeState.setHasVotedInThisTerm(false);
    }

    public String createRequestVoteRPCObject() {
        Message message = new Message();
        message.setRequest(NodeConstants.REQUEST.VOTE_REQUEST.toString());
        message.setTerm(nodeState.getTerm());
//        message.setSender_name(nodeState.getNodeValue().toString());
        message.setSender_name(nodeState.getNodeName());
        message.setPrevLogIndex(nodeState.getLastApplied());
        Gson gson = new Gson();
        String voteRequestMessage = gson.toJson(message);
        LOGGER.info("VOTE REQ OBJECT BY:" + nodeState.getNodeName() + "; message: " + voteRequestMessage);
        return voteRequestMessage;
    }

    public void sendPacketToAll(String message) {
        try {
            byte[] buf = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket new_packet;
            for (String add : NodeInfo.addresses) {
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        } catch (Exception ex) {
            LOGGER.info("EXCEPTION CAUSED IN SENDING PACKET: " + ex.getMessage());
        }
    }


    public void sendPacketToNode(String message, InetAddress to_address, int to_port) {
        byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, to_address, to_port);
        try {
            socket.send(packetToSend);
        } catch (Exception exception) {
            LOGGER.error("sendMessageToNode.message: " + message + ", Error: ", exception);
        }
    }

    public void sendPacketToAllExceptSelf(String message) {
        try {
            byte[] buf = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket new_packet;
            for (String add : NodeInfo.addresses) {
                if (add.equalsIgnoreCase(nodeState.getNodeName()))
                    continue;
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        } catch (Exception ex) {
            LOGGER.info("EXCEPTION CAUSED IN SENDING PACKET: " + ex.getMessage());
        }
    }

    public Message createVoteResponse(Message voteRequestMessage) {
        Integer requestTerm = voteRequestMessage.getTerm();

        Message responseMessage = new Message();
        responseMessage.setRequest(NodeConstants.REQUEST.VOTE_ACK.toString());
        responseMessage.setSender_name(nodeState.getNodeName());
        responseMessage.setTerm(requestTerm);

        LOGGER.info("createVoteResponse: has voted in this term: " + nodeState.getHasVotedInThisTerm()
                + ", requestTerm: " + requestTerm + ", nodeState.getTerm(): " + nodeState.getTerm()
                + ", voteRequestMessage.getPrevLogIndex(): " + voteRequestMessage.getPrevLogIndex()
                + ", nodeState.getLastApplied(): " + nodeState.getLastApplied());
        if (nodeState.getHasVotedInThisTerm()) {
            nodeState.setTerm(requestTerm);
            nodeState.setHasVotedInThisTerm(true);
            responseMessage.setValue("0");
        } else if (requestTerm > nodeState.getTerm()) {
            nodeState.setTerm(requestTerm);
            nodeState.setHasVotedInThisTerm(true);
            responseMessage.setValue("1");
        } else if (requestTerm.equals(nodeState.getTerm()) && voteRequestMessage.getPrevLogIndex() < nodeState.getLastApplied()) {
            LOGGER.info("Candidate node is behind the voting: " + voteRequestMessage.getPrevLogIndex());
            nodeState.setTerm(requestTerm);
            nodeState.setHasVotedInThisTerm(true);
            responseMessage.setValue("0");

        } else {
            nodeState.setHasVotedInThisTerm(true);
            responseMessage.setValue("0");
        }
        LOGGER.info("createVoteResponse.responseMessage: " + responseMessage + " sending to " + voteRequestMessage.getSender_name());

        /*else {
            if (!nodeState.getHasVotedInThisTerm()) {
                hasVoted = true;
                nodeState.setHasVotedInThisTerm(true);
            }
        }
        LOGGER.info("Vote requested by: " + voteRequestMessage.getSender_name() + "; Vote done by: " + nodeState.getNodeValue() + "; Has it voted? " + hasVoted);
        int value = 0;
        if (hasVoted) {
            value = 1;
        }
        responseMessage.setValue(String.valueOf(value));*/
        return responseMessage;
    }

    public Message createAcknowledgeLeaderRequest() {
        Message leaderMessage = new Message();
        leaderMessage.setSender_name(nodeState.getNodeName());
        leaderMessage.setTerm(nodeState.getTerm());
        leaderMessage.setRequest(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.toString());
        return leaderMessage;
    }

    public String createLeaderInfo() {
        Gson gson = new Gson();
        Message message = new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setRequest(NodeConstants.REQUEST.LEADER_INFO.toString());
        message.setValue(nodeState.getCurrentLeader());
        LOGGER.info(gson.toJson(message));
        return gson.toJson(message);
    }

    public Message createShutdownRequest() {
        Message message = new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setRequest(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString());
        return message;
    }

    public Entry createEntryFromStoreRequest(Message receivedMessage) {
        Entry entry = new Entry();
        entry.setTerm(nodeState.getTerm().toString());
        entry.setKey(receivedMessage.getKey());
        entry.setValue(receivedMessage.getValue());
        return entry;
    }

    public String createRetrieveMessage() {
        Gson gson = new Gson();
        Message message = new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setKey(NodeConstants.REQUEST.COMMITTED_LOGS.toString());
        message.setRequest(NodeConstants.REQUEST.RETRIEVE.toString());
        ArrayList<Entry> committed_logs = new ArrayList<>();
        for (int i = 0; i < nodeState.getCommitIndex(); i++)
            committed_logs.add(nodeState.getEntries().get(i));
        String entries = gson.toJson(committed_logs);
        message.setValue(entries);
        LOGGER.info("Retrieve message: " + gson.toJson(message));
        return gson.toJson(message);
    }

    public String createHeartBeatMessage(String address) {
        Gson gson = new Gson();
        Message message = new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setRequest(NodeConstants.REQUEST.APPEND_ENTRY.toString());
        message.setTerm(nodeState.getTerm());

        Integer matchIndex = NodeState.getNodeState().getMatchIndex().get(address);
            Integer nextIndex = NodeState.getNodeState().getNextIndex().get(address);
        if (nodeState.getEntries().size() > nextIndex - 1) {
            LOGGER.info("createHeartBeatMessage.nextIndex: " + nextIndex + ", nodeState.getEntries().size(): "+nodeState.getEntries().size());
            String logMessage = gson.toJson(NodeState.getNodeState().getEntries().get(nextIndex - 1));
            message.setLog(Arrays.asList(logMessage));
        }

        message.setPrevLogIndex(matchIndex);
        message.setPrevLogTerm(NodeState.getNodeState().getPrevLogTerm());
        message.setCommitIndex(NodeState.getNodeState().getCommitIndex());

        String heartbeatMessage = gson.toJson(message);
        LOGGER.info("Sending heartbeat: " + heartbeatMessage);
        return heartbeatMessage;
    }

    public String createAppendReply(boolean reply) {
        Gson gson = new Gson();
        nodeState.setPrevLogTerm(nodeState.getTerm());
        Message message = new Message();
        message.setTerm(nodeState.getTerm());
        message.setRequest(NodeConstants.REQUEST.APPEND_REPLY.toString());
        message.setSender_name(nodeState.getNodeName());
        message.setValue(String.valueOf(reply));
        message.setSuccess(reply);
        message.setMatchIndex(getNodeState().getLastApplied());
        String appendReplyMessage = gson.toJson(message);
        LOGGER.info("createAppendReply.message: " + appendReplyMessage);
        return appendReplyMessage;
    }
}

