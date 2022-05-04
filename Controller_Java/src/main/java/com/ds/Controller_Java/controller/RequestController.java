package com.ds.Controller_Java.controller;

import com.ds.Controller_Java.config.SocketConfig;
import com.ds.Controller_Java.model.Message;
import com.ds.Controller_Java.service.impl.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/")
public class RequestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestController.class.getName());

    @Autowired
    private SocketConfig socketConfig;

    @Autowired
    private RequestService requestService;

    @GetMapping("/get-leader-data")
    public ResponseEntity<Void> getLeaderData(){
        try{
            requestService.getLeaderInfo();
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (Exception ex){
            LOGGER.error("getLeaderData.Error: ",ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send-store-request")
    public ResponseEntity<Void> sendStoreRequest(@RequestBody Message message){
        try{
            requestService.sendStoreRequest(message);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (Exception ex){
            LOGGER.error("sendStoreRequest.Error: ",ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send-shutdown-message")
    public ResponseEntity<Void> sendShutDownMessage(@RequestBody Message message){
        try{
            requestService.sendShutDownMessage(message);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (Exception ex){
            LOGGER.error("sendShutDownMessage.Error: ",ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/send-message")
    public ResponseEntity<Void> sendMessage(@RequestBody Message message){
        try{
            requestService.sendMessage(message);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (Exception ex){
            LOGGER.error("sendMessage.Error: ",ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}

