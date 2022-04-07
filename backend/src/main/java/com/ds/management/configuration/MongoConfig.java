package com.ds.management.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.logging.Logger;


@Configuration
public class MongoConfig {

    private final static Logger LOGGER = Logger.getLogger(MongoConfig.class.getName());

    @Autowired
    private Environment env;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private String port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${node.value}")
    private String nodeValue;


    @Bean
    public MongoClient mongo() {

        LOGGER.info("mongo.host: "+host);
        LOGGER.info("mongo.port: "+port);
        LOGGER.info("nodeValue: "+nodeValue);

        StringBuilder con = new StringBuilder();
        con.append("mongodb://").append(host).append(":").append(port).append("/").append(database);

        ConnectionString connectionString = new ConnectionString(con.toString());

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), "test");
    }

}