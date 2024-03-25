package com.github.alantr7.prepo.dto.logs;

public class IssuePOJO {
    
    public String title;

    public String description;

    public String project_name;

    public int index;

    public IssuePOJO(String title, String description, String project_name, int index) {
        this.title = title;
        this.description = description;
        this.project_name = project_name;
        this.index = index;
    }

}
