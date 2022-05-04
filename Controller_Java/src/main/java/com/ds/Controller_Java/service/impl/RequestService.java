package com.ds.Controller_Java.service.impl;

import com.ds.Controller_Java.model.Message;

public interface RequestService {
    void getLeaderInfo() throws Exception;

    void sendStoreRequest(Message message) throws Exception;

    void sendShutDownMessage(Message request) throws Exception;

    void sendMessage(Message request) throws Exception;

}
