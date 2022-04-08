package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import lombok.Data;

import javax.annotation.PostConstruct;
import java.util.Random;

@Data
public class NodeState {

    private static NodeState node= null;

    private Integer timeout;
    private Integer heartbeat;
    private NodeConstants.SERVER_STATE server_state;
    private String nodeValue;
    private Integer votedFor;
    private Boolean hasVotedInThisTerm;
    private Integer term;

    private NodeState(){
        int base= 150;
        Random random= new Random();
        heartbeat= random.nextInt(150)+ base;
        timeout= this.heartbeat*2;
        server_state= NodeConstants.SERVER_STATE.FOLLOWER;
        nodeValue = NodeInfo.NODE_VALUE;
        votedFor= 0;
        hasVotedInThisTerm= false;
        term= 0;
    }

    public static NodeState getNodeState(){
        if(node==null){
            node= new NodeState();
        }
        return node;
    }

}
