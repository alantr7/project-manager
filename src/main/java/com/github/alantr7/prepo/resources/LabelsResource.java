package com.github.alantr7.prepo.resources;

import java.util.Collection;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.github.alantr7.prepo.entity.ProjectIssueLabelEntity;

@Resource
@Path("/api/v1/labels")
public class LabelsResource {
    
    @GET
    public Collection<ProjectIssueLabelEntity> getAllLabels() {
        return ProjectIssueLabelEntity.listAll();
    }

}
