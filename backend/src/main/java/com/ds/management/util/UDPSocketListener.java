package com.ds.management.util;

import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import com.ds.management.models.NodeState;
import com.ds.management.models.RequestVoteRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.net.InetAddress;

public class UDPSocketListener implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocketListener.class);

    private boolean isRunning;
    private byte[] buf = new byte[256];
    private DatagramSocket socket;
    private NodeState nodeState;

    public UDPSocketListener() throws SocketException {
        socket= new DatagramSocket(NodeInfo.port);
        nodeState= NodeState.getNodeState();
        socket.setSoTimeout(nodeState.getTimeout());
        LOGGER.info("node: "+nodeState.toString());
    }

    public void run() {
        isRunning = true;
        try {
            LOGGER.info("Running: "+nodeState.getNodeValue());
            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                //LOGGER.info("Listener: BEFORE receive");
                socket.receive(packet);

                //LOGGER.info("Listener: AFTER receive");

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                LOGGER.info("Msg : "+ received+ "Received: "+ address.getHostName()+ "; Current: "+ nodeState.getNodeValue());
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
