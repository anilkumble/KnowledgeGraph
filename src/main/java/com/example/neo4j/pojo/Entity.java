package com.example.neo4j.pojo;

public class Entity
{
    private String name;
    private String type;
    private double score;

    public Entity(String name, String type, double score)
    {
        this.name   =   name;
        this.type   =   type;
        this.score  =   score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", score=" + score +
                '}';
    }
}
