package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.models.NodeState;
import com.ds.management.models.RequestVoteRPC;
import com.ds.management.models.ResponseVoteRPC;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

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
        socket.setSoTimeout(20000);
        LOGGER.info("socket Info: "+socket.getLocalPort());
    }

    public void listen() {
        isRunning = true;
        while (isRunning) {
            try {
                    LOGGER.info("Test " + socket.isClosed());
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException ex) {
                        LOGGER.info("ARE YOU HEREEEEEE? STUPID FUCK!!");
                        getReadyForElection();
                    }
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, 6060);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    LOGGER.info("Msg : \n" + receivedMessage);
                    understandMessage(receivedMessage);
                    //socket.send(packet);
            } catch (Exception ex) {
                LOGGER.info("Exception: ", ex);
            }
        }
        socket.close();
    }

    public void understandMessage(String message){
        Gson gson= new Gson();
        Map<String, String> map = gson.fromJson(message, new TypeToken<HashMap<String, Object>>() {}.getType());
        LOGGER.info("map: \n"+ map.toString());
        String type_of_message= "";
        if(map.containsKey("type")){
            type_of_message= map.get("type");
        }
        switch (type_of_message){
            case "VOTE_REQUEST":
                LOGGER.info("vote request asked");
                break;
            case "VOTE_RESPONSE":
                LOGGER.info("vote response received");
                break;
            case "HEARTBEAT":
                LOGGER.info("vote response received");
                break;
        }
        LOGGER.info("GDUYHUHWUD.........");
    }

    public void getReadyForElection(){
        try{
            if(nodeState.getServer_state()== NodeConstants.SERVER_STATE.FOLLOWER){
                updateNodeState();
                String requestVoteRPC= createRequestVoteRPCObject();

            }
        }
        catch (Exception ex){

        }
    }

    public void updateNodeState(){
        int new_term= nodeState.getTerm()+1;
        nodeState.setTerm(new_term);
        nodeState.setServer_state(NodeConstants.SERVER_STATE.CANDIDATE);
    }

    public String createRequestVoteRPCObject(){
        RequestVoteRPC voteObject= new RequestVoteRPC();
        voteObject.setCandidateId(NodeState.getNodeState().getNodeValue());
        voteObject.setTerm(nodeState.getTerm());
        voteObject.setType(NodeConstants.REQUEST.VOTE_REQUEST);
        Gson gson= new Gson();
        String voteRequestMessage= gson.toJson(voteObject);
        return voteRequestMessage;
    }

    public String vote_for(RequestVoteRPC requestVoteRPC){
        ResponseVoteRPC response= new ResponseVoteRPC();
        response.setType(NodeConstants.REQUEST.VOTE_RESPONSE);
        response.setNodeValue(nodeState.getNodeValue());
        if(requestVoteRPC.getTerm() > nodeState.getTerm()){
            nodeState.setTerm(requestVoteRPC.getTerm());
            nodeState.setHasVotedInThisTerm(true);
            response.setHasVoted(true);
        }
        else{
            if(nodeState.getHasVotedInThisTerm()){
                response.setHasVoted(false);
            }else{
                response.setHasVoted(true);
            }
        }
        Gson gson= new Gson();
        String response_msg= gson.toJson(response);
        return response_msg;
    }

}
