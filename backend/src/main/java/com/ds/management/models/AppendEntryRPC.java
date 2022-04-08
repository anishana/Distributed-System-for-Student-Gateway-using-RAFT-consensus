package com.ds.management.models;

import com.ds.management.constants.NodeConstants;
import lombok.Data;

import java.util.List;

/*
* leaderId: ID of the leader
* Entries [] : Log entries to store (Empty for heartbeats)
* prevLogIndex: index of the log entry preceding new entries.
* prevLogTerm : term of prevLogIndex entry
*
* */
@Data
public class AppendEntryRPC {

    private String leaderId;
    private NodeConstants.REQUEST type;
    private Integer term;

}
