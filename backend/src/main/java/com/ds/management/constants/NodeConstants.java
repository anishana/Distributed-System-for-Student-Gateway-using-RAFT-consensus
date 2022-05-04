package com.ds.management.constants;

public class NodeConstants {
    public enum SERVER_STATE {FOLLOWER, CANDIDATE, LEADER}

    public enum REQUEST {
        APPEND_ENTRY,
        VOTE_REQUEST,
        VOTE_ACK,
        ACKNOWLEDGE_LEADER,
        CONVERT_FOLLOWER,
        LEADER_INFO,
        SHUTDOWN,
        SHUTDOWN_PROPAGATE,
        TIMEOUT,
        STORE,
        RETRIEVE,
        COMMITTED_LOGS
    }

    public final static String LEADER_KEY = "LEADER";
}