package com.ds.management.models;

import lombok.Data;

@Data
public class ResponseVoteRPC {

    private Integer type;
    private Integer votedBy;
    private Integer term;

}
