package com.ds.management.constants;

public class NodeConstants {
    public enum SERVER_STATE{FOLLOWER, CANDIDATE, LEADER};

    public enum REQUEST{HEARTBEAT, VOTE_REQUEST, VOTE_RESPONSE, ACKNOWLEDGE_LEADER};

}