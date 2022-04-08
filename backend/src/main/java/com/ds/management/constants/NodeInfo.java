package com.ds.management.constants;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NodeInfo {
    public static final Integer port = 5555;

    public static String NODE_VALUE = "";

    public static List<String> addresses = new ArrayList<String>(List.of(new String[]{"Node1", "Node2", "Node3"}));
//    public static List<String> addresses= new ArrayList<String>(List.of(new String[]{"localhost"}));

    public static Integer totalNodes = 5;

    public static Integer majorityNodes = 3;

}
