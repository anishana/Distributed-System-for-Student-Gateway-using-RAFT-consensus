package com.ds.management.models;

import lombok.Data;

@Data
public class AcknowledgeLeader {
    Integer type;
    Integer term;
    String leaderId;
}
