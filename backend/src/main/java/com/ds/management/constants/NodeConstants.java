package com.ds.management.constants;

import java.util.HashMap;

public class NodeConstants {
    public enum SERVER_STATE {FOLLOWER, CANDIDATE, LEADER}

    public enum REQUEST {
        HEARTBEAT,
        VOTE_REQUEST,
        VOTE_ACK,
        ACKNOWLEDGE_LEADER,
        CONVERT_FOLLOWER,
        LEADER_INFO,
        ACKNOWLEDGE_LEADER_INFO,
        SHUTDOWN,
        SHUTDOWN_PROPAGATE,
        TIMEOUT,
        STORE,
        RETRIEVE,
        COMMITTED_LOGS,
        APPEND_ENTRY,
        APPEND_REPLY,
        RESUME
    }

    public final static String LEADER_KEY = "LEADER";

}