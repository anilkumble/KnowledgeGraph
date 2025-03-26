package com.example.neo4j.service;

import com.example.neo4j.pojo.Entity;
import com.example.neo4j.pojo.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeExtractor
{
    @Autowired
    private HuggingFaceService huggingFaceService;

    @Autowired
    private ReplicateService replicateService;

    public List<Entity> extractEntities(String input) throws Exception
    {
        return huggingFaceService.extractEntities(input);
    }

    public List<Relationship> extractRelationships(String input) throws Exception
    {
        List<Relationship> relationships    =   huggingFaceService.extractRelationships(input);
        relationships.addAll( replicateService.extractRelationships(input) );

        return relationships.stream().distinct().collect(Collectors.toList());
    }

}
