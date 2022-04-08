package com.ds.management.models;

import lombok.Data;

@Data
public class ResponseVoteRPC {

    private Integer type;
    private String votedBy;
    private String votedFor;
    private Boolean hasVoted;
    private Integer term;

}
