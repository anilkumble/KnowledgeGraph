package com.example.neo4j.service;

import com.example.neo4j.pojo.Entity;
import com.example.neo4j.pojo.Relationship;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Neo4jService
{
    @Autowired
    @Qualifier("neo4jDriver")
    private Driver neo4jDriver;

    public void buildKnowledgeGraph(List<Entity> entities, List<Relationship> relationships)
    {
        Map<String, Entity> entityMap   =   new HashMap<>();
        for(Entity entity : entities)
        {
            entityMap.put(entity.getName(), entity);
        }

        this.storeEntities(entities);

        this.storeRelation(entityMap, relationships);
    }

    private void storeEntities(List<Entity> entities)
    {
        try (Session session = neo4jDriver.session())
        {
            for (Entity entity : entities)
            {
                String query = "MERGE (e:" + entity.getType() + " {name: $name})" +
                        " ON CREATE SET e.name = $name, e.type = $type, e.score = $score" +
                        " RETURN e;";

                Map<String, Object> params  =   new HashMap<>();
                params.put("name", entity.getName());
                params.put("type", entity.getType());
                params.put("score", entity.getScore());

                Result result   =   session.run(query, params);

                System.out.println("Entity stored or updated: " + entity.getName() + " (" + entity.getType() + ")");
            }
        }
    }

    private void createEntity(String type, String name)
    {
        try (Session session = neo4jDriver.session())
        {
            String query = "MERGE (e:" + type + " {name: $name})" +
                    " ON CREATE SET e.name = $name, e.type = $type" +
                    " RETURN e;";

            Map<String, Object> params  =   new HashMap<>();
            params.put("name", name);
            params.put("type", type);

            Result result   =   session.run(query, params);

            System.out.println("Entity stored or updated: " + name + " (" + type + ")");
        }
    }

    private void storeRelation(Map<String, Entity> entityMap, List<Relationship> relationships)
    {
        try (Session session = neo4jDriver.session())
        {
            for (Relationship relationship : relationships)
            {
                Entity entity1  =   entityMap.get(relationship.getEntity1());
                Entity entity2  =   entityMap.get(relationship.getEntity2());

                Map<String, Object> params  =   new HashMap<>();
                params.put("name1", relationship.getEntity1());
                params.put("name2", relationship.getEntity2());

                if(entity1 != null && entity2 != null)
                {
                    String query    =   "MERGE (e1:"+entity1.getType()+" {name: $name1})" +
                            " MERGE (e2:"+entity2.getType()+" {name: $name2})" +
                            " MERGE (e1)-[r:"+ relationship.getRelation() +"]->(e2)" +
                            " RETURN e1, r, e2;";

                    session.run(query, params);
                }
                else
                {
                    String query    =   this.getRelQueryForUndefinedEntity(entity1, entity2, relationship);

                    session.run(query, params);
                }

                System.out.println("Relationship created or updated: " + relationship.getEntity1() + " ( " + relationship.getRelation() + " ) " + relationship.getEntity2());
            }

        }
    }

    private String getRelQueryForUndefinedEntity(Entity entity1, Entity entity2, Relationship relationship)
    {
        String entity1Type  =   "";
        String entity2Type  =   "";

        if(entity1 == null)
        {
            this.createEntity("MISC", relationship.getEntity1());
            entity1Type =   "MISC";
        }
        else
        {
            entity1Type =   entity1.getType();
        }

        if(entity2 == null)
        {
            this.createEntity("MISC", relationship.getEntity2());
            entity2Type =   "MISC";
        }
        else
        {
            entity2Type =   entity2.getType();
        }

        return "MERGE (e1:"+ entity1Type +" {name: $name1})" +
                " MERGE (e2:"+entity2Type+" {name: $name2})" +
                " MERGE (e1)-[r:"+ relationship.getRelation() +"]->(e2)" +
                " RETURN e1, r, e2;";
    }

}
