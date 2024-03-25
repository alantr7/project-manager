package com.github.alantr7.prepo.resources;

import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.github.alantr7.prepo.dto.ProjectBuildDTO;
import com.github.alantr7.prepo.entity.ProjectBuildEntity;
import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectFileEntity;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.entity.UserNotificationEntity;
import com.github.alantr7.prepo.entity.ProjectBuildEntity.BuildChannel;
import com.github.alantr7.prepo.entity.UserNotificationEntity.Type;
import com.github.alantr7.prepo.util.LogUtils;

import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.vertx.core.json.JsonArray;

@Resource
@Path("/api/v1/projects/{project}/builds")
public class BuildsResource {

    @RequestScoped
    @PathParam("project")
    String projectId;

    @Inject
    LogUtils logs;

    @GET
    @Transactional
    public Object getAll(@QueryParam("channel") String channelId) {
        if (channelId == null)
            return ProjectBuildEntity.<ProjectBuildEntity>find("project_id = ?1", Sort.descending("id"), projectId)
                    .stream()
                    .map(ProjectBuildDTO::new)
                    .collect(Collectors.toList());

        BuildChannel channel;
        if (channelId.equals("development")) {
            channel = BuildChannel.DEVELOPMENT;
        }
        else if (channelId.equals("release")) {
            channel = BuildChannel.RELEASE;
        } else {
            return Response.status(400).entity("invalid_channel").build();
        }

        return ProjectBuildEntity.<ProjectBuildEntity>find("project_id = ?1 AND channel = ?2", Sort.descending("id"), projectId, channel)
                    .stream()
                    .map(ProjectBuildDTO::new)
                    .collect(Collectors.toList());
    }

    @GET
    @Path("/{build}")
    public Object get(@PathParam("build") long buildId) {
        var build = ProjectBuildEntity.findById(buildId);
        return build;
    }

    @GET
    @Path("/autochangelog")
    public Object getAutoChangelog() {
        var query = ProjectIssueEntity.getEntityManager().createNativeQuery(
                "select * from issues iss where `isResolved` = true and project_id = :projectId and (select count(*) from builds_changelogs changel where changel.issue_id = iss.id) = 0",
                ProjectIssueEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    @POST
    @Transactional
    @Authenticated
    public Object create(@Context SecurityContext ctx, @NotNull @FormParam("files") String filesJson,
            @NotNull @FormParam("changes") String changesJson, @FormParam("version") String versionParam) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        if (project == null)
            return Response.status(404).build();

        String version;
        if (versionParam == null) {
            version = project.getVersion();
        } else {
            if (!versionParam.trim().matches("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$(a|b|)")) {
                return Response.status(400).entity("invalid_version_value").build();
            }

            version = versionParam.trim();
        }

        var filesArr = new JsonArray(filesJson);
        var changesArr = new JsonArray(changesJson);

        var build = project.createBuild();
        var author = UserEntity.<UserEntity>findById(ctx.getUserPrincipal().getName());

        for (var fileId : filesArr) {
            var file = ProjectFileEntity.<ProjectFileEntity>findById(((String) fileId));
            if (file == null)
                return Response.status(400).build();

            build.getFiles().add(file);
        }

        for (var changeRaw : changesArr) {
            if (changeRaw instanceof String) {
                build.createChange((String) changeRaw);
            } else {
                var file = ProjectIssueEntity.<ProjectIssueEntity>findById(((Integer) changeRaw).longValue());
                if (file == null)
                    return Response.status(400).build();

                build.createChange(file);
            }
        }

        build.setVersion(version);
        build.setAuthor(author);

        UserNotificationEntity.create(Type.NEW_BUILD, build, UserEntity.listAll());
        logs.createEntryForBuildCreate(author, build);

        project.setVersion(version);

        return build;
    }

    @Transactional
    @DELETE @Path("/{id}")
    public Object delete(@Context SecurityContext ctx, @NotNull @PathParam("id") long buildId) {
        var build = ProjectBuildEntity.<ProjectBuildEntity>findById(buildId);
        if (build == null)
            return Response.status(404).build();

        if (!build.getAuthor().getId().equals(ctx.getUserPrincipal().getName()))
            return Response.status(403).build();

        build.delete();
        return Response.status(200).build();
    }

}
