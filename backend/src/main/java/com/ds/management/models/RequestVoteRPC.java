package com.ds.management.models;

import lombok.Data;

@Data
public class RequestVoteRPC {
    private String sender_name;
    private String request;
    private Integer term;
    private String key;
    private String value;
    private String candidateId;
    private String lastLogTerm;

}
