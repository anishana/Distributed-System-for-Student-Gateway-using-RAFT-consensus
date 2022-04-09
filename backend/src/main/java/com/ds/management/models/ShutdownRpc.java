package com.ds.management.models;

import lombok.Data;

@Data
public class ShutdownRpc {
    Integer type;
    String request;
    String nodeId;
}
