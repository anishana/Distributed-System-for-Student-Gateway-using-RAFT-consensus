package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import lombok.Data;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    private Integer numberOfVotes;
    private Set<String> votedBy;
    private Boolean isLeader;
    private String currentLeader;

    private NodeState(){
        int base= 5000;
        Random random= new Random();
        heartbeat= random.nextInt(5000)+ base;
        timeout= this.heartbeat*2;
        server_state= NodeConstants.SERVER_STATE.FOLLOWER;
        nodeValue = NodeInfo.NODE_VALUE;
        votedFor= 0;
        hasVotedInThisTerm= false;
        term= 0;
        votedBy= new HashSet<>();
        numberOfVotes=0;
        isLeader= false;
        currentLeader= "";
    }

    public static NodeState getNodeState(){
        if(node==null){
            node= new NodeState();
        }
        return node;
    }

}
