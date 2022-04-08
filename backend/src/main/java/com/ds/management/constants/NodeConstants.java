package com.ds.management.constants;

import java.util.HashMap;

public class NodeConstants {
    public enum SERVER_STATE {FOLLOWER, CANDIDATE, LEADER}

    public enum REQUEST {
        HEARTBEAT,
        VOTE_REQUEST,
        VOTE_RESPONSE,
        ACKNOWLEDGE_LEADER,
        CONVERT_FOLLOWER,
        LEADER_INFO,
        ACKNOWLEDGE_LEADER_INFO,
        SHUTDOWN
    }

    public final static String LEADER_KEY = "LEADER";
}