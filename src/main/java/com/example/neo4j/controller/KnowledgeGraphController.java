package com.example.neo4j.controller;

import com.example.neo4j.pojo.ApiResponse;
import com.example.neo4j.service.AsyncProcessor;
import com.example.neo4j.service.HuggingFaceService;
import com.example.neo4j.service.Neo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class KnowledgeGraphController {

    @Autowired
    private HuggingFaceService huggingFaceService;

    @Autowired
    private Neo4jService neo4jService;

    @Autowired
    private AsyncProcessor asyncProcessor;

    @PostMapping("/knowledge-graph")
    public ResponseEntity<ApiResponse> buildKnowledgeGraph(@RequestBody Map<String, Object> requestData) throws Exception
    {
        asyncProcessor.buildKnowledgeGraph((String) requestData.get("input"));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new ApiResponse("Accepted", "Your request has been accepted and is being processed. Please wait for completion."));
    }
}
