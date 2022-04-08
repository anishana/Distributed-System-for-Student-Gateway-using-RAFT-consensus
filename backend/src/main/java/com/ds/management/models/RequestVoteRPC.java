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

    private NodeConstants.REQUEST type;
    private String candidateId;
    private Integer term;

}
