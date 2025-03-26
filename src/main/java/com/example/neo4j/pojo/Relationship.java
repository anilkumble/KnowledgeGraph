package com.example.neo4j.pojo;

public class Relationship
{
    private String entity1;
    private String relation;
    private String entity2;

    public Relationship(String entity1, String relation, String entity2)
    {
        this.entity1    =   entity1;
        this.entity2    =   entity2;
        this.relation   =   relation.replace(" ", "_").toUpperCase();
    }

    public String getEntity1() {
        return entity1;
    }

    public void setEntity1(String entity1) {
        this.entity1 = entity1;
    }

    public String getRelation()
    {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getEntity2() {
        return entity2;
    }

    public void setEntity2(String entity2) {
        this.entity2 = entity2;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "entity1='" + entity1 + '\'' +
                ", relation='" + relation + '\'' +
                ", entity2='" + entity2 + '\'' +
                '}';
    }
}
