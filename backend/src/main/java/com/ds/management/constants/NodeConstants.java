package com.ds.management.constants;

public class NodeConstants {
    public enum SERVER_STATE{FOLLOWER, CANDIDATE, LEADER};

    public enum CLIENT_REQUEST{HEARTBEAT, VOTE};

    public enum SERVER_REQUEST{HEARTBEAT, APPEND_ENTRY};

};

