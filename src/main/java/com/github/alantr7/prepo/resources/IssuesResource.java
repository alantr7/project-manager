package com.github.alantr7.prepo.resources;

import com.github.alantr7.prepo.dto.ProjectIssueDTO;
import com.github.alantr7.prepo.dto.ProjectIssueTaskDTO;
import com.github.alantr7.prepo.entity.BoardListCard;
import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectFileEntity;
import com.github.alantr7.prepo.entity.ProjectIssueAttachmentEntity;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.ProjectIssueLabelEntity;
import com.github.alantr7.prepo.entity.ProjectIssueTaskEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.util.LogUtils;
import com.github.alantr7.prepo.util.WhereQuery;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.vertx.core.json.JsonArray;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.reactive.RestResponse.Status;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Resource
@Path("/api/v1/projects/{project}/issues")
public class IssuesResource {

    @Context
    @RequestScoped
    SecurityContext context;

    @PathParam("project")
    String projectId;

    @Inject
    LogUtils logs;

    @GET
    @Transactional
    public Object getIssues(@PathParam("project") String projectId,
                            @QueryParam("closed") @DefaultValue("false") boolean closed, @QueryParam("q") String query,
                            @QueryParam("labels") String labelsParam, @QueryParam("assignees") String assigneesParam,
                            @QueryParam("logged") Boolean isLogged) {

        var issues = ProjectIssueEntity.find("project_id = ?1 and isResolved = ?2", Sort.descending("creationDate"), projectId, closed).project(ProjectIssueDTO.class).<ProjectIssueDTO>list();

        var tasksQuery = ProjectIssueTaskEntity.getEntityManager().createQuery(
                "SELECT NEW com.alantrumic.ednevnik.dto.ProjectIssueTaskDTO(t.id, t.text, t.creationDate, t.isCompleted, t.issue.id) FROM issue_tasks t WHERE t.issue.project.id = :projectId", ProjectIssueTaskDTO.class)
                .setParameter("projectId", projectId);

        var labels = (List<Object[]>) ProjectIssueLabelEntity.getEntityManager().createNativeQuery(
                "SELECT * FROM issues_labels LEFT JOIN labels ON issues_labels.issue_id = labels.id"
        ).<Object[]>getResultList();

        var attachmentsQuery = ProjectIssueAttachmentEntity.getEntityManager().createQuery(
                "SELECT a FROM issue_attachments a WHERE a.issue.project.id = :projectId"
        ).setParameter("projectId", projectId);

        var tasks = tasksQuery.<Object[]>getResultList();
        var attachments = (List<ProjectIssueAttachmentEntity>) attachmentsQuery.getResultList();

        issues.forEach(issue -> {
            tasks.stream().filter(task -> Objects.equals(task.issueId, issue.id)).forEach(issue.tasks::add);
            labels.stream().filter(label -> ((BigInteger) label[0]).longValue() == (issue.id)).forEach(label -> {
                var map = new HashMap<String, Object>();
                map.put("id", label[1]);
                map.put("title", label[5]);
                map.put("backgroundColor", label[3]);
                map.put("textColor", label[4]);

                issue.labels.add(map);
            });
            issue.attachments = attachments.stream().filter(a -> Objects.equals(a.issue.id, issue.id)).map(a -> a.file).collect(Collectors.toList());
        });

        return issues;
    }

    @GET
    @Path("/legacy")
    @Transactional
    public Object getIssuesLegacy(@PathParam("project") String projectId,
                                  @QueryParam("closed") @DefaultValue("false") boolean closed, @QueryParam("q") String query,
                                  @QueryParam("labels") String labelsParam, @QueryParam("assignees") String assigneesParam,
                                  @QueryParam("logged") Boolean isLogged) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        if (project == null)
            return Collections.emptyList();

        if (query != null && query.length() < 3) {
            return Response.status(400).entity("query must be at least 3 characters long").build();
        }

        var conditionsBuilder = new WhereQuery();
        conditionsBuilder.put("project_id = %n", projectId);
        conditionsBuilder.put("isResolved = %n", closed);

        var filters = new ArrayList<Predicate<ProjectIssueEntity>>();

        if (labelsParam != null && labelsParam.length() > 0) {
            // Parse parameters
            var labelsArray = labelsParam.contains(";") ? labelsParam.split(";") : new String[]{labelsParam};
            var labels = new ArrayList<String>();

            for (var label : labelsArray) {
                if (!label.matches("[a-zA-Z0-9_]+"))
                    return Response.status(400).build();

                labels.add(label);
            }

            // TODO: Remove labels filter, and add it to query instead!
            filters.add(issue -> {
                for (var label : labels) {
                    for (var issueLabel : issue.getLabels()) {
                        if (issueLabel.getId().equals(label))
                            return true;
                    }
                }
                return false;
            });
        }

        if (assigneesParam != null && assigneesParam.length() > 0) {
            // Parse parameters
            var assigneesArray = assigneesParam.contains(";") ? assigneesParam.split(";") : new String[]{assigneesParam};
            var assignees = new ArrayList<String>();

            for (var label : assigneesArray) {
                if (!label.matches("[a-zA-Z0-9_]+"))
                    return Response.status(400).build();

                assignees.add(label);
            }
/* 
            filters.add(issue -> {
                for (var label : assignees) {
                    for (var issueLabel : issue.getLabels()) {
                        if (issueLabel.getId().equals(label))
                            return true;
                    }
                }
                return false;
            });*/
        }

        if (query != null) {
            conditionsBuilder.put("title like %n or description like %n", "%" + query + "%");
        }

        if (isLogged != null) {
            conditionsBuilder.put("id not in (select id from builds_changelogs)");
        }

        return ProjectIssueEntity
                .<ProjectIssueEntity>find(conditionsBuilder.getQuery(), conditionsBuilder.getParameters())
                .<ProjectIssueEntity>list().stream()
                .filter(issue -> {
                    for (var predicate : filters)
                        if (!predicate.test(issue))
                            return false;

                    return true;
                })
                .collect(Collectors.toList());

    }

    @POST
    @Transactional
    @Authenticated
    public Object createIssue(@Context SecurityContext context, @PathParam("project") String projectId,
                              @NotNull @FormParam("title") String title, @NotNull @FormParam("description") String description,
                              @FormParam("labels") String labelJson, @FormParam("tasks") String tasksJson,
                              @FormParam("attachments") String attachmentsJson) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var principal = context.getUserPrincipal();
        var author = UserEntity.<UserEntity>findById(principal.getName());
        var issue = project.createIssue(author, title.trim());
        issue.setDescription(description.trim());

        if (labelJson != null) {
            try {
                var jsonArray = ((List<String>) new JsonArray(labelJson).getList());
                issue.getLabels().clear();

                jsonArray.forEach(id -> {
                    var label = ProjectIssueLabelEntity.<ProjectIssueLabelEntity>findById(id);
                    if (label == null)
                        return;

                    issue.addLabel(label);
                });
            } catch (Exception ignored) {
            }
        }

        if (tasksJson != null) {
            try {
                var jsonArray = ((List<LinkedHashMap>) new JsonArray(tasksJson).getList());
                var tasks = new ArrayList<ProjectIssueTaskEntity>();

                for (var jsonObj : jsonArray) {
                    var text = (String) jsonObj.get("text");
                    var completed = (boolean) jsonObj.getOrDefault("completed", false);

                    var task = new ProjectIssueTaskEntity(issue);
                    task.setText(text);
                    task.setCompleted(completed);

                    issue.getTasks().add(task);
                    task.persist();
                }
            } catch (Exception ignored) {
                return Response.status(400).build();
            }
        }

        if (attachmentsJson != null) {
            try {
                var jsonArray = ((List<String>) new JsonArray(attachmentsJson).getList());
                var attachments = new ArrayList<ProjectIssueAttachmentEntity>();

                jsonArray.forEach(id -> {
                    var file = ProjectFileEntity.<ProjectFileEntity>findById(id);
                    if (file == null)
                        return;

                    var attachment = new ProjectIssueAttachmentEntity(issue, file);
                    attachments.add(attachment);

                    attachment.persist();
                });

                issue.setAttachments(attachments);
            } catch (Exception ignored) {
            }
        }

        var entry = logs.createEntryForIssueOpen(author, issue);
        if (entry != null) {
            entry.persist();
        }

        return issue;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Authenticated
    public Object updateIssue(@PathParam("project") String projectId, @PathParam("id") long issueId,
                              @FormParam("title") String title, @FormParam("description") String description,
                              @FormParam("labels") String labelJson) {
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);
        if (issue == null)
            return Response.status(404).build();

        if (title != null)
            issue.setTitle(title.trim());

        if (description != null)
            issue.setDescription(description.trim());

        if (labelJson != null) {
            try {
                var jsonArray = ((List<String>) new JsonArray(labelJson).getList());
                issue.getLabels().clear();

                jsonArray.forEach(id -> {
                    var label = ProjectIssueLabelEntity.<ProjectIssueLabelEntity>findById(id);
                    if (label == null)
                        return;

                    issue.addLabel(label);
                });
            } catch (Exception ignored) {
            }
        }

        return Response.ok(issue).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Authenticated
    public Object deleteIssue(@PathParam("id") long issueId) {
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);
        if (issue == null)
            return Response.status(Status.NOT_FOUND).build();

        issue.getAttachments().forEach(file -> file.getBinary().delete());

        issue.delete();
        return Response.ok();
    }

    @PUT
    @Path("/{id}/status")
    @Transactional
    @Authenticated
    public Object setStatus(@PathParam("id") long issueId, @FormParam("state") @NotNull ProjectIssueEntity.State state) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (issue == null)
            return Response.status(404).build();

        // TODO: Check if the status is same as the current

        if (state == ProjectIssueEntity.State.RESOLVED) {
            logs.createEntryForIssueClose(issue.getAuthor(), issue);
            BoardListCard.find("issue_id = ?1", issueId).firstResultOptional().ifPresent(PanacheEntityBase::delete);
        } else {
            logs.createEntryForIssueOpen(issue.getAuthor(), issue);
        }

        issue.setState(state);
        return Response.ok(issue).build();
    }

    @PUT
    @Path("/{id}/labels")
    @Transactional
    @Authenticated
    public Object putLabel(@PathParam("project") String projectId, @PathParam("id") long issueId,
                           @FormParam("label") String labelId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);
        var label = ProjectIssueLabelEntity.<ProjectIssueLabelEntity>findById(labelId);

        if (project == null || issue == null || label == null) {
            return Response.status(404).entity("not_found").build();
        }

        if (issue.getLabels().stream().anyMatch(issueLabel -> issueLabel.getId().equalsIgnoreCase(labelId))) {
            return Response.status(Status.NOT_MODIFIED).build();
        }

        issue.addLabel(label);
        return Response.status(Status.ACCEPTED).build();
    }

    @DELETE
    @Path("/{id}/labels/{label}")
    @Transactional
    @Authenticated
    public Object removeLabel(@PathParam("project") String projectId, @PathParam("id") long issueId,
                              @PathParam("label") String labelId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (issue == null)
            return Response.status(Status.NOT_FOUND).header("Content", "issue_not_found").build();

        var label = ProjectIssueLabelEntity.<ProjectIssueLabelEntity>findById(labelId);
        issue.removeLabel(label);

        return Response.status(Status.OK).build();
    }

    @PUT
    @Path("/{id}/attachments")
    @Transactional
    @Authenticated
    public Object putAttachment(@PathParam("project") String projectId, @PathParam("id") long issueId,
                                @NotNull @FormParam("id") String fileId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (issue == null)
            return Response.status(Status.NOT_FOUND).header("Content", "issue_not_found").build();

        var file = ProjectFileEntity.<ProjectFileEntity>findById(fileId);
        if (file == null)
            return Response.status(Status.NOT_FOUND).header("Content", "file_not_found").build();

        var attachment = new ProjectIssueAttachmentEntity(issue, file);
        attachment.persist();

        issue.addAttachment(attachment);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/tasks")
    @Transactional
    public Object createTask(@PathParam("project") String projectId, @PathParam("id") long issueId,
                             @FormParam("text") String text) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (issue == null)
            return Response.status(Status.NOT_FOUND).header("Content", "issue_not_found").build();

        if (text == null)
            return Response.status(Status.BAD_REQUEST).header("Content", "text_not_provided").build();

        var task = new ProjectIssueTaskEntity(issue);
        task.setText(text);

        task.persist();
        issue.getTasks().add(task);

        return task;
    }

    @PUT
    @Path("/{issueId}/tasks/{taskId}")
    @Transactional
    public Object updateTask(@PathParam("issueId") long issueId, @PathParam("taskId") String taskId,
                             @FormParam("text") String text, @FormParam("completed") Boolean completed) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (text == null && completed == null) {
            return Response.status(Status.NOT_MODIFIED).build();
        }

        if (issue == null)
            return Response.status(Status.NOT_FOUND).header("Content", "issue_not_found").build();

        var task = ProjectIssueTaskEntity.<ProjectIssueTaskEntity>findById(taskId);
        if (task == null)
            return Response.status(404).build();

        if (text != null) {
            task.setText(text);
        }

        if (completed != null) {
            task.setCompleted(completed);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/{issueId}/tasks/{taskId}")
    @Transactional
    public Object deleteTask(@PathParam("project") String projectId, @PathParam("issueId") long issueId,
                             @PathParam("taskId") String taskId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(issueId);

        if (issue == null)
            return Response.status(Status.NOT_FOUND).header("Content", "issue_not_found").build();

        var task = ProjectIssueTaskEntity.<ProjectIssueTaskEntity>findById(taskId);
        if (task == null)
            return Response.status(404).build();

        task.delete();
        return Response.ok().build();
    }

}
