package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import com.ds.management.constants.NodeInfo;
import lombok.Data;

import java.util.*;

@Data
public class NodeState {

    private static NodeState node = null;

    private Integer timeout;
    private Integer heartbeat;
    private NodeConstants.SERVER_STATE server_state;
    private Integer nodeValue;
    private String nodeName;
    private Boolean hasVotedInThisTerm;
    private Integer numberOfVotes;
    private Boolean isLeader;

    //persistent state on all servers
    private Integer term;
    private String votedFor;
    private List<Entry> entries;

    //volatile state on all servers
    private Integer commitIndex;
    private Integer lastApplied;

    //volatile state on leaders
    private Map<String, Integer> nextIndex;
    private Map<String, Integer> matchIndex;

    private NodeState() {
        Random random = new Random();
        heartbeat = random.nextInt(NodeInfo.base_rate) + NodeInfo.base_rate;
        timeout = this.heartbeat * 2;
        server_state = NodeConstants.SERVER_STATE.FOLLOWER;
        nodeValue = NodeInfo.NODE_VALUE;
        nodeName= "Node"+ nodeValue;
        hasVotedInThisTerm = false;
        numberOfVotes = 0;
        isLeader = false;

        term = 0;
        votedFor = "";
        entries= new ArrayList<>();
    }

    public static NodeState getNodeState() {
        if (node == null) {
            node = new NodeState();
        }
        return node;
    }

}
