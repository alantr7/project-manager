package com.github.alantr7.prepo.dto.logs;

public class BuildPOJO {

    public String title;
    
    public String version;

    public String project_name;

    public BuildPOJO(String title, String version, String project_name) {
        this.title = title;
        this.version = version;
        this.project_name = project_name;
    }

}
