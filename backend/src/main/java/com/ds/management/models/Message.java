package com.ds.management.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Message {
    private String sender_name;
    private String request;
    private Integer term;
    private String key;
    private String value;
}
