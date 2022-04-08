package com.ds.management.constants;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NodeInfo {
    public static final Integer port= 6060;

    public static String NODE_VALUE= "";

    public static List<String> addresses= new ArrayList<String>(List.of(new String[]{"api-server-1", "api-server-2", "api-server-3"}));
//    public static List<String> addresses= new ArrayList<String>(List.of(new String[]{"localhost"}));

    public static Integer totalNodes=5;

    public static Integer majorityNodes=3;

}
