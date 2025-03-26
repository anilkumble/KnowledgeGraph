package com.example.neo4j.configuration;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;

public class DriverConfiguration
{
    @Bean("neo4jDriver")
    public Driver neo4jDriver() {
        return GraphDatabase.driver("neo4j://localhost:7687", AuthTokens.basic("neo4j", "rootpassword"));
    }
}
