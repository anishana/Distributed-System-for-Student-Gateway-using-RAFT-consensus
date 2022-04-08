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
import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Component
public class UDPSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private byte[] buf = new byte[1024];
    private DatagramSocket socket;
    private NodeState nodeState;

    @Autowired
    private SocketConfig socketConfig;

    @Value("${is.Master}")
    private String isMaster;

    @PostConstruct
    public void setNodeState() throws SocketException {
        socket= socketConfig.socket();
        nodeState= NodeState.getNodeState();
        socket.setSoTimeout(nodeState.getTimeout());
        LOGGER.info("socket Info: "+socket.getLocalPort());
    }

    public void listen() {
        isRunning = true;
        while (isRunning) {
            try {
                DatagramPacket packet_received = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet_received);
                } catch (SocketTimeoutException ex) {
                    LOGGER.info("ARE YOU HEREEEEEE? STUPID FUCK!!");
                    getReadyForElection();
                }
                String receivedMessage = new String(packet_received.getData(), 0, packet_received.getLength());
//                LOGGER.info("Message received: "+receivedMessage);
                understandMessage(receivedMessage, packet_received);
//                InetAddress from_address = packet_received.getAddress();
//                int from_port = packet_received.getPort();
//                DatagramPacket packetToBeSent = new DatagramPacket(buf, buf.length, address, 6060);
//                socket.send(packet);
            }
            catch (Exception ex) {
                LOGGER.info("Exception: ", ex);
            }
        }
        socket.close();
    }

    public void understandMessage(String message, DatagramPacket receivedPacket){
        JSONObject jsonObject= new JSONObject(message);
        int type= Integer.parseInt(jsonObject.get("type").toString());
        if(type== NodeConstants.REQUEST.HEARTBEAT.ordinal()){
            LOGGER.info("Heartbeat.");
        }
        else if (type== NodeConstants.REQUEST.VOTE_REQUEST.ordinal()){
            LOGGER.info("Vote Request.");
            voteRequested(message, receivedPacket);
        }
        else if (type == NodeConstants.REQUEST.VOTE_RESPONSE.ordinal()){
            LOGGER.info("!!Vote Response!!");
            updateVoteResponse(message);
        }
        else if(type == NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal()){
            LOGGER.info("!!ACKNOWLEDGE LEADER!!");
            setLeader(message);
        }
    }

    public void getReadyForElection(){
        try{
            if(nodeState.getServer_state()== NodeConstants.SERVER_STATE.FOLLOWER){
                updateNodeState();
                String requestVoteRPC= createRequestVoteRPCObject();
                buf= requestVoteRPC.getBytes(StandardCharsets.UTF_8);
                DatagramPacket new_packet;
                for(String add: NodeInfo.addresses){
                    InetAddress address= InetAddress.getByName(add);
                    LOGGER.info("VOTE REQ OBJECT: address: "+address.toString()+"; message: "+requestVoteRPC);
                    new_packet= new DatagramPacket(buf, buf.length, address, 6060);
                    socket.send(new_packet);
                }
            }
        }
        catch (Exception ex){
            LOGGER.info("EXCEPTION RAISED WHILE ELECTION: "+ ex.getMessage());
        }
    }

    public void updateNodeState(){
        int new_term= nodeState.getTerm()+1;
        nodeState.setTerm(new_term);
        nodeState.setServer_state(NodeConstants.SERVER_STATE.CANDIDATE);
        nodeState.setHasVotedInThisTerm(false);
        nodeState.setNumberOfVotes(0);
        nodeState.setVotedFor(-1);
        nodeState.setVotedBy(new HashSet<>());
    }

    public String createRequestVoteRPCObject(){
        RequestVoteRPC voteObject= new RequestVoteRPC();
        voteObject.setCandidateId(NodeState.getNodeState().getNodeValue());
        voteObject.setTerm(nodeState.getTerm());
        voteObject.setType(NodeConstants.REQUEST.VOTE_REQUEST.ordinal());
        JSONObject jsonObject= new JSONObject(voteObject);
        String voteRequestMessage= jsonObject.toString();
        LOGGER.info("VOTE REQ OBJECT: "+voteRequestMessage);
        return voteRequestMessage;
    }

    public void voteRequested(String receivedMessage, DatagramPacket receivedPacket){
        JSONObject obj= new JSONObject(receivedMessage);
        String candidateId= obj.get("candidateId").toString();
        Integer request_term= Integer.parseInt(obj.get("term").toString());

        ResponseVoteRPC response= new ResponseVoteRPC();
        response.setType(NodeConstants.REQUEST.VOTE_RESPONSE.ordinal());
        response.setVotedBy(nodeState.getNodeValue());
        response.setTerm(request_term);

        if(request_term > nodeState.getTerm()){
            nodeState.setTerm(request_term);
            nodeState.setHasVotedInThisTerm(true);
            response.setHasVoted(true);
            response.setVotedFor(candidateId);
        }
        else{
            if(nodeState.getHasVotedInThisTerm()){
                response.setHasVoted(false);
            }else{
                response.setHasVoted(true);
                response.setVotedFor(candidateId);
            }
        }
        if(response.getHasVoted()){
            JSONObject voteResponse= new JSONObject(response);
            buf= voteResponse.toString().getBytes(StandardCharsets.UTF_8);
            InetAddress from_address = receivedPacket.getAddress();
            int from_port = receivedPacket.getPort();
            DatagramPacket packetToSend= new DatagramPacket(buf, buf.length, from_address, from_port);
            try{
                socket.send(packetToSend);
            }catch (Exception exception){
                LOGGER.info("EXCEPTION WHILE SENDING VOTE RESPONSE; vote for:"+candidateId+"; voted by: "+response.getVotedBy());
            }
        }
    }

    public void updateVoteResponse(String message){
        LOGGER.info("VOTE RESPONSE: "+message);
        JSONObject object= new JSONObject(message);
        int requestTerm= Integer.parseInt(object.get("term").toString());
        String votedBy= object.get("votedBy").toString();
        LOGGER.info("VOTED BY: "+votedBy+"; SET:"+nodeState.getVotedBy());
        if((requestTerm== nodeState.getTerm()) && !(nodeState.getVotedBy().contains(votedBy))){
            int currentVotes= nodeState.getNumberOfVotes();
            nodeState.setNumberOfVotes(currentVotes+1);
            nodeState.getVotedBy().add(votedBy);
            if(nodeState.getNumberOfVotes()>=NodeInfo.majorityNodes){
                LOGGER.info("THE WINNER IS: "+nodeState.getNodeValue());
                //TO DO-> write a function which shall
                //acknowledge the winner
                acknowledgeLeader();
            }
        }
    }

    public void acknowledgeLeader(){
        JSONObject obj= new JSONObject();
        obj.put("type", NodeConstants.REQUEST.ACKNOWLEDGE_LEADER.ordinal());
        obj.put("term", nodeState.getTerm());
        obj.put("leaderId", nodeState.getNodeValue());
        String message= obj.toString();
        buf= message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket new_packet;
        try {
            for (String add : NodeInfo.addresses) {
                InetAddress address = InetAddress.getByName(add);
                new_packet = new DatagramPacket(buf, buf.length, address, 6060);
                socket.send(new_packet);
            }
        }
        catch (Exception exception){
            LOGGER.info("EXCEPTION!! ACKNOWLEDGE THE WINNER!!!!");
        }
    }

    public void setLeader(String message){
        JSONObject object= new JSONObject(message);
        String leader= object.get("leaderId").toString();
        Integer term= Integer.parseInt(object.get("term").toString());
        if(leader.equalsIgnoreCase(nodeState.getNodeValue())){
            nodeState.setIsLeader(true);
        }else{
            nodeState.setIsLeader(false);
        }
        nodeState.setCurrentLeader(leader);
        nodeState.setTerm(term);
        LOGGER.info("Current node: "+nodeState.getNodeValue()+ "; leader : "+ nodeState.getCurrentLeader());
    }

}
