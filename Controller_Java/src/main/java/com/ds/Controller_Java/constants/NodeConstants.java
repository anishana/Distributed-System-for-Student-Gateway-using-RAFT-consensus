package com.ds.Controller_Java.constants;

public class NodeConstants {
    public static StringBuffer leaderName = new StringBuffer();
    public static Integer clusterPort = 5555;
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
        APPEND_REPLY
    }

}
