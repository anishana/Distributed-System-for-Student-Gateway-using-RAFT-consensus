package com.ds.management.util;

import com.ds.management.configuration.SocketConfig;
import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.NodeState;
import com.ds.management.models.RequestVoteRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.InetAddress;

//@Service
public class UDPSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private byte[] buf = new byte[256];
    private DatagramSocket socket;
    private NodeState nodeState;


    public UDPSocketListener() throws SocketException {
        socket= new DatagramSocket(NodeInfo.port);
//        socket.setSoTimeout(nodeState.getTimeout());
//        LOGGER.info("node: "+nodeState.toString());
    }

    @PostConstruct
    public void setNodeState() throws SocketException {
//        socket= socketConfig.socket();
//        socket= new DatagramSocket(NodeInfo.port);
        nodeState= NodeState.getNodeState();
        socket.setSoTimeout(nodeState.getTimeout());

    }

    public void test(){
        LOGGER.info("Nothing doing");
    }

    public void listen() {
        isRunning = true;
        LOGGER.info("Running: "+NodeState.getNodeState().getNodeValue());
        try {

            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                //LOGGER.info("Listener: BEFORE receive");
                socket.receive(packet);

                //LOGGER.info("Listener: AFTER receive");

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                LOGGER.info("Msg : "+ received+ "Received: "+ address.getHostName()+ "; Current: "+ NodeState.getNodeState().getNodeValue());
                socket.send(packet);
            }
            socket.close();
        }
        catch (SocketTimeoutException ex){
            getReadyForElection();
        }
        catch (Exception ex){
            LOGGER.info("Exception: ", ex);
        }
    }

    public void getReadyForElection(){
        try{
            if(nodeState.getServer_state()== NodeConstants.SERVER_STATE.FOLLOWER){
                nodeState.setServer_state(NodeConstants.SERVER_STATE.CANDIDATE);
                RequestVoteRPC requestVoteRPC= createRequestVoteRPCObject();
            }

        }
        catch (Exception ex){

        }
    }

    public RequestVoteRPC createRequestVoteRPCObject(){
        NodeInfo.TERM= NodeInfo.TERM+1;
        RequestVoteRPC voteObject= new RequestVoteRPC();
        voteObject.setSender_name(String.valueOf(NodeInfo.NODE_VALUE));
        voteObject.setTerm(NodeInfo.TERM);
        return voteObject;
    }
/*
    public Integer ask_for_votes(){

    }

    public Integer vote_for(RequestVoteRPC requestVoteRPC){

    }*/

}
