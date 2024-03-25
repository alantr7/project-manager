package com.github.alantr7.prepo.resources;

import com.alantrumic.ednevnik.entity.*;
import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.util.LogUtils;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.ProjectMilestoneEntity;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.*;

@Resource
@Path("/api/v1/projects/{project}/milestones")
public class MilestonesResource {

    @Context
    @RequestScoped
    SecurityContext context;

    @PathParam("project")
    String projectId;

    @Inject
    LogUtils logs;

    @GET
    @Transactional
    public Object getMilestones() {
        return ProjectMilestoneEntity.find("project_id = ?1", Sort.descending("creationDate"), projectId).list();
    }

    @POST
    @Transactional
    @Authenticated
    public Object createMilestone(@Context SecurityContext context, @PathParam("project") String projectId,
                                  @NotNull @FormParam("name") String name,
                                  @NotNull @FormParam("version") String version
    ) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var milestone = new ProjectMilestoneEntity(project);
        milestone.setCreationDate(Date.from(Instant.now()));
        milestone.setName(name);
        milestone.setVersion(version);

        milestone.persist();
        return milestone;
    }

    @DELETE @Path("/{id}")
    @Transactional
    @Authenticated
    public Object deleteMilestone(@PathParam("id") String milestoneId) {
        var milestone = ProjectMilestoneEntity.<ProjectMilestoneEntity>findById(milestoneId);
        milestone.delete();

        return Response.status(200).build();
    }

    @POST @Path("/{id}/goals")
    @Transactional
    @Authenticated
    public Object addIssueToMilestone(@PathParam("id") String milestoneId, @FormParam("issue") Long issueId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var milestone = ProjectMilestoneEntity.<ProjectMilestoneEntity>findById(milestoneId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        issue.setMilestone(milestone);
        return Response.status(200).build();
    }

}
