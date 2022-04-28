package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import lombok.Data;

/*
To be added:
1. lastLogIndex
2. lastLogTerm
 */

@Data
public class RequestVoteRPC {

    private Integer request;
    private Integer candidateId;
    private Integer term;

}
