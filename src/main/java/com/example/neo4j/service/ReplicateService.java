package com.example.neo4j.service;

import com.example.neo4j.pojo.Relationship;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ReplicateService
{
    private static final String REPLICATE_API_URL = "https://api.replicate.com/v1/predictions";
    private static final String REPLICATE_API_TOKEN = "r8_2kZ3D1HRexCQL2rLHOvqiw51RQAe7zC4GWeBD";

    public List<Relationship> extractRelationships(String text)
    {
        try
        {
            URL url                         =   new URL(REPLICATE_API_URL);
            HttpURLConnection connection    =   (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Token " + REPLICATE_API_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String prompt       =   String.format("<|SYSTEM|>\\nYou are a relationship extraction assistant. Return relationships in this format comma separated (entity1, relation, entity2) in new line. Don't give me as bulletins .\\n<|END_SYSTEM|>\\nExtract all relationships from the \\nText: %s", text);
            String requestBody  =   String.format("{\"version\": \"meta/llama-2-7b-chat\", \"input\": {\"prompt\": \"%s\"}}", prompt);

            try (OutputStream os = connection.getOutputStream())
            {
                os.write(requestBody.getBytes());
            }

            ObjectMapper mapper     =   new ObjectMapper();
            String responseId       =   mapper.readTree(connection.getInputStream()).get("id").asText();
            ArrayNode outputNode    =   fetchAndParseResults(responseId);

            return postProcessResult(outputNode);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println("error in extracting relationship from Replicate Service: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private ArrayNode fetchAndParseResults(String predictionId) throws Exception
    {
        ObjectMapper mapper =   new ObjectMapper();
        String statusUrl    =   REPLICATE_API_URL + "/" + predictionId;

        while (true) {
            URL url                         =   new URL(statusUrl);
            HttpURLConnection connection    =   (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + REPLICATE_API_TOKEN);

            JsonNode response   =   mapper.readTree(connection.getInputStream());
            String status       =   response.get("status").asText();

            if (!("starting".equals(status)) && response.get("output") != null)
            {
                return (ArrayNode)response.get("output");
            }
            else if ("failed".equals(status)) {
                throw new RuntimeException("Prediction failed");
            }

            System.out.println("Status: " + status + " - Waiting for completion...");
            TimeUnit.SECONDS.sleep(10);
        }
    }

    private List<Relationship> postProcessResult(ArrayNode outputNode)
    {
        StringBuilder builder               =   new StringBuilder();
        List<Relationship> relationships    =   new ArrayList<>();

        for (JsonNode word : outputNode)
        {
            builder.append(word.asText());
        }

        String text     =   builder.toString();
        String[] lines  =   text.split("\\n");

        for (String line : lines)
        {
            System.out.println("line - " + line);

            if (line.contains(","))
            {
                if(line.contains("(") && line.contains(")"))
                {
                    line    =   line.replaceAll("\\(", "");
                    line    =   line.replaceAll("\\)", "");
                }

                this.buildRelationshipPojo(relationships, line);
            }
        }
        return relationships;
    }

    private void buildRelationshipPojo(List<Relationship> relationships, String data)
    {
        String[] relSplits  =   data.split(",");
        if(relSplits.length == 3)
        {
            relationships.add(new Relationship(relSplits[0], relSplits[1], relSplits[2]));
        }
    }
}
