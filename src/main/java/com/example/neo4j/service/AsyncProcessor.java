package com.example.neo4j.service;

import com.example.neo4j.pojo.Entity;
import com.example.neo4j.pojo.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsyncProcessor {


    @Autowired
    private KnowledgeExtractor extractor;

    @Autowired
    private Neo4jService neo4jService;

    @Async
    public void buildKnowledgeGraph(String input)
    {
        try
        {
            System.out.println("Started entity extraction" );
            List<Entity> entities               =   extractor.extractEntities(input);
            System.out.println("Completed entity extraction, Entities :: " + entities);

            System.out.println("Started relationship extraction" );
            List<Relationship> relationships    =   extractor.extractRelationships(input);
            System.out.println("Completed relationship extraction, Relationships :: " + relationships);

            System.out.println("Started KnowledgeGraph construction");
            neo4jService.buildKnowledgeGraph(entities, relationships);
            System.out.println("Completed KnowledgeGraph construction");
        }
        catch (Exception e)
        {
            System.err.println("Data processing interrupted: " + e.getMessage());
        }
    }
}
