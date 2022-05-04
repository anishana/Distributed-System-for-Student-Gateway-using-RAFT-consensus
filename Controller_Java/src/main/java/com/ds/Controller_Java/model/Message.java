package com.ds.Controller_Java.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
    private String receiver;
}
