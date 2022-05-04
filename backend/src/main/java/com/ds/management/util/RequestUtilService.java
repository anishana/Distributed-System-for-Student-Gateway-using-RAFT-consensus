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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestUtilService {

    @Value("${node.value}")
    private String nodeVal;

    @Autowired
    private SocketConfig socketConfig;

    private static NodeState nodeState= NodeState.getNodeState();

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestUtilService.class);

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

    public void sendPacketToAll(byte[] buf){
        try{
            DatagramPacket new_packet;
            DatagramSocket socket= socketConfig.socket();
            for (String add : NodeInfo.addresses) {
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        }catch (Exception ex){
            LOGGER.info("EXCEPTION CAUSED IN SENDING PACKET: "+ex.getMessage());
        }
    }

    public void sendPacketToAllExceptSelf(byte[] buf){
        try{
            DatagramPacket new_packet;
            DatagramSocket socket= socketConfig.socket();
            for (String add : NodeInfo.addresses) {
                if(add.equalsIgnoreCase(nodeState.getNodeName()))
                    continue;
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, NodeInfo.port);
                socket.send(new_packet);
            }
        }catch (Exception ex){
            LOGGER.info("EXCEPTION CAUSED IN SENDING PACKET: "+ex.getMessage());
        }
    }

    public Message createVoteResponse(Message voteRequestMessage){
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
        return responseMessage;
    }

    public Message createAcknowledgeLeaderRequest(){
        Message leaderMessage= new Message();
        leaderMessage.setSender_name(nodeState.getNodeValue().toString());
        leaderMessage.setTerm(nodeState.getTerm());
        leaderMessage.setRequest(NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.toString());
        return leaderMessage;
    }

    public void createLeaderInfo() {
        Gson gson= new Gson();
        Message message= new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setRequest(NodeConstants.REQUEST.LEADER_INFO.toString());
        message.setValue(nodeState.getNodeName());
        LOGGER.info(gson.toJson(message));
    }

    public Message createShutdownRequest(){
        Message message= new Message();
        message.setSender_name(nodeVal);
        message.setRequest(NodeConstants.REQUEST.SHUTDOWN_PROPAGATE.toString());
        return message;
    }

    public Entry createEntryFromStoreRequest(Message receivedMessage){
        Entry entry= new Entry();
        entry.setTerm(nodeState.getTerm().toString());
        entry.setKey(receivedMessage.getKey());
        entry.setValue(receivedMessage.getValue());
        return entry;
    }

    public void createRetrieveMessage(){
        Gson gson= new Gson();
        Message message= new Message();
        message.setSender_name(nodeState.getNodeName());
        message.setKey(NodeConstants.REQUEST.COMMITTED_LOGS.toString());
        String entries= gson.toJson(nodeState.getEntries());
        message.setValue(entries);
        LOGGER.info("Retrieve message: "+ gson.toJson(message));
    }

    public String createHeartBeatMessage(){
        Gson gson= new Gson();
        Message message= new Message();
        message.setSender_name(nodeState.getNodeValue().toString());
        message.setRequest(NodeConstants.REQUEST.APPEND_ENTRY.toString());
        message.setTerm(nodeState.getTerm());
        String heartbeatMessage= gson.toJson(message);
        LOGGER.info("Sending heartbeat: " + heartbeatMessage);
        return heartbeatMessage;
    }


}
