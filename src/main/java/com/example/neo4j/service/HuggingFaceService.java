package com.example.neo4j.service;

import com.example.neo4j.pojo.Entity;
import com.example.neo4j.pojo.Relationship;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class HuggingFaceService {

    private static final String HUGGING_FACE_URL    =   "https://api-inference.huggingface.co/models/";
    private static final String HUGGING_FACE_TOKEN  =   "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";

    public List<Entity> extractEntities(String text) throws Exception
    {
        URL url                         =   new URL(HUGGING_FACE_URL + "dbmdz/bert-large-cased-finetuned-conll03-english");
        HttpURLConnection connection    =   (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + HUGGING_FACE_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

//        String prompt       =   String.format("<|SYSTEM|>\\nYou are a relationship extraction assistant. Return relationships in in the format (e1,rel,e2) in new line. Strictly avoid explanations like 'Sure, Below find results like that'.\\n<|END_SYSTEM|>\\nExtract all relationships from the \\nText: %s", text);
        String requestBody  =   String.format("{\"inputs\": \"%s\", \"options\": {\"wait_for_model\": true}}", text);

        try (OutputStream os = connection.getOutputStream())
        {
            os.write(requestBody.getBytes());
        }

        ObjectMapper mapper     =   new ObjectMapper();
        ArrayNode results       =   (ArrayNode) mapper.readTree(connection.getInputStream());

        return postProcessEntities(results);
    }

    public List<Relationship> extractRelationships(String text)
    {
        try
        {
            URL url                         =   new URL(HUGGING_FACE_URL + "Babelscape/rebel-large");
            HttpURLConnection connection    =   (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + HUGGING_FACE_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

//        String prompt       =   String.format("<|SYSTEM|>\\nYou are a relationship extraction assistant. Return relationships in in the format (e1,rel,e2) in new line. Strictly avoid explanations like 'Sure, Below find results like that'.\\n<|END_SYSTEM|>\\nExtract all relationships from the \\nText: %s", text);
            String requestBody  =   String.format("{\"inputs\": \"%s\", \"options\": {\"wait_for_model\": true}}", text);

            try (OutputStream os = connection.getOutputStream())
            {
                os.write(requestBody.getBytes());
            }

            ObjectMapper mapper     =   new ObjectMapper();
            ArrayNode results       =   (ArrayNode) mapper.readTree(connection.getInputStream());

            return postProcessRelationships(results);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println("error in extracting relationship from Hugging Face Service: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Relationship> postProcessRelationships(ArrayNode results)
    {
        List<Relationship> result   =   new ArrayList<>();

        if(results.size() == 1 && results.get(0).get("generated_text") != null)
        {
            String output   =   results.get(0).get("generated_text").asText();
            String[] tokens =   output.split("\\s{2,}");

            if (tokens.length % 3 != 0)
            {
                System.out.println("Unexpected input format.");
                return result;
            }
            for (int i = 0; i < tokens.length; i += 3)
            {
                result.add(new Relationship(tokens[i].trim(), tokens[i + 2].trim(), tokens[i + 1].trim()));
            }
        }

        return result;
    }

    private List<Entity> postProcessEntities(ArrayNode results)
    {
        List<Entity> entities   =   new ArrayList<>();

        for(JsonNode result : results)
        {
            String type     =   result.get("entity_group").asText();
            double score    =   result.get("score").asDouble();
            String name     =   result.get("word").asText();
            entities.add(new Entity(name, type, score));
        }

        return entities;
    }

}
