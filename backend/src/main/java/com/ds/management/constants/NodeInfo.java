package com.ds.management.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NodeInfo {
    public static final Integer port= 6060;

    public static Integer NODE_VALUE=0;

    public static Integer TERM=0;

//    public static List<String> addresses= new ArrayList<String>(List.of(new String[]{"api-server-1", "api-server-2", "api-server-3"}));
    public static List<String> addresses= new ArrayList<String>(List.of(new String[]{"localhost"}));

}
