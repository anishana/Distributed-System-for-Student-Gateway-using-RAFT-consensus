package com.ds.management.models;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Message {
    private String sender_name;
    private String request;
    private Integer term;
    private String key;
    private String value;
    private List<String> log;
    private Integer prevLogTerm;
    private Integer prevLogIndex;
    private Integer commitIndex;
    private Integer success;
    private Entry entry;
}
