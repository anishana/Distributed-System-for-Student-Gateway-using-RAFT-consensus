package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import lombok.Data;

@Data
public class ResponseVoteRPC {

    private NodeConstants.REQUEST type;
    private String nodeValue;
    private Boolean hasVoted;

}
