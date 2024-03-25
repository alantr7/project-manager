package com.github.alantr7.prepo.resources;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.github.alantr7.prepo.dto.ListGroupsGroupDTO;
import com.github.alantr7.prepo.dto.ListProjectsProjectDTO;
import com.alantrumic.ednevnik.entity.*;

import com.github.alantr7.prepo.entity.*;
import io.quarkus.security.Authenticated;

@Resource
@Path("/api/v1/workspaces")
public class WorkspacesResource {

    @RequestScoped
    @Inject
    Principal principal;

    @GET
    @Transactional
    @Authenticated
    public Object getAll(@Context SecurityContext context) {
        var author = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        return WorkspaceEntity.getEntityManager().createQuery(
                "SELECT w FROM workspaces w LEFT JOIN workspaces_collaborators wc ON wc.workspace.id = w.id WHERE wc.user.id = :userId OR w.owner.id = :userId"
        ).setParameter("userId", author.getId()).getResultList();
    }

    @POST
    @Path("/{workspace}/projects")
    @Transactional
    @Authenticated
    public Object createProject(@NotNull @PathParam("workspace") String weakId, @NotNull @FormParam("name") String name, @FormParam("group") Long groupId, @FormParam("version") String version) {
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", weakId).firstResult();
        if (workspace == null)
            return Response.status(404).build();

        var id = ProjectEntity.getIdFromName(name);
        var project = new ProjectEntity(id);
        project.setWorkspace(workspace);
        project.setName(name);

        if (groupId != null) {
            project.setGroup(groupId == 0 ? null : ProjectGroupEntity.<ProjectGroupEntity>findById(groupId));
        }

        if (version != null) {
            if (!version.trim().matches("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$(a|b|)")) {
                return Response.status(400).entity("invalid_version_value").build();
            }

            project.setVersion(version);
        }

        project.persist();
        return project;
    }

    @GET
    @Transactional
    @Path("/{workspace}/projects")
    @Produces("application/json")
    public Object listProjects(@PathParam("workspace") @NotNull String weakId, @QueryParam("group") Boolean group) {
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", weakId).firstResult();
        if (workspace == null)
            return Response.status(404).build();

        if (group != null && group) {
            List<Object> groups = new ArrayList<>(ProjectGroupEntity.<ProjectGroupEntity>find("workspace_id = ?1", workspace.id).list());
            groups.add(ProjectGroupEntity.DEFAULT);

            groups.replaceAll(group1 -> new ListGroupsGroupDTO(workspace, (ProjectGroupEntity) group1));
            return groups;
        }

        var list = new ArrayList<>();
        ProjectEntity.<ProjectEntity>streamAll().forEach(item -> list.add(new ListProjectsProjectDTO(item)));

        return list;
    }

    @POST
    @Transactional
    @Authenticated
    public WorkspaceEntity createWorkspace(@FormParam("name") @NotNull String name) {
        var user = UserEntity.<UserEntity>findById(principal.getName());
        var workspace = new WorkspaceEntity(name, user);

        workspace.persist();
        workspace.setWeakId(workspace.getName().replace(" ", "-").toLowerCase() + "-" + workspace.id);

        return workspace;
    }

    @POST
    @Transactional
    @Authenticated
    @Path("/{workspace}/invitations")
    public Object createInvitation(@Context SecurityContext context, @PathParam("workspace") String workspaceId, @FormParam("user") @NotNull String email) {
        var author = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        var user = UserEntity.<UserEntity>find("email = ?1", email).firstResult();
        if (user == null)
            return Response.status(404).build();

        if (WorkspaceInvitationEntity.find("user_id = ?1 AND workspace_id = ?2", user.getId(), workspace.id).firstResult() != null)
            return Response.status(Response.Status.NOT_MODIFIED).build();

        var invitation = new WorkspaceInvitationEntity(workspace, author, user);
        invitation.persist();

        return invitation;
    }

    @GET
    @Transactional
    @Path("/{workspace}/members")
    public Object getMembers(@PathParam("workspace") String workspaceId) {
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        if (workspace == null)
            return Response.status(404).build();

        var map = new LinkedHashMap<String, Object>();

        var collabs = WorkspaceCollaboratorEntity.getEntityManager().createQuery("SELECT wc.user FROM workspaces_collaborators wc WHERE wc.workspace.id = :workspaceId")
                .setParameter("workspaceId", workspace.id)
                .getResultList();

        var invitations = WorkspaceInvitationEntity.getEntityManager().createQuery("SELECT wi.user FROM workspaces_invitations wi WHERE wi.workspace.id = :workspaceId")
                .setParameter("workspaceId", workspace.id)
                .getResultList();

        map.put("members", collabs);
        map.put("pending", invitations);
        map.put("blocked", Collections.emptyList());

        return map;
    }

    @DELETE
    @Transactional
    @Authenticated
    @Path("/{workspace}/members/{user}")
    public Object kickMember(@Context SecurityContext context, @PathParam("workspace") String workspaceId, @PathParam("user") String userId) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        var collaborator = WorkspaceCollaboratorEntity.<WorkspaceCollaboratorEntity>find("user_id = ?1", userId).firstResult();

        if (collaborator == null)
            return Response.status(404).build();

        if (!user.getId().equals(workspace.getOwner().getId()))
            return Response.status(403).build();

        collaborator.delete();
        return Response.status(200).build();
    }

    @POST
    @Transactional
    @Authenticated
    @Path("/{workspace}/invitations/{invitation}/accept")
    public Object acceptInvitation(@Context SecurityContext context, @PathParam("invitation") Long invitationId, @PathParam("workspace") String workspaceId) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        var invitation = WorkspaceInvitationEntity.<WorkspaceInvitationEntity>findById(invitationId);

        if (invitation == null)
            return Response.status(404).build();

        if (!user.getId().equals(invitation.getUser().getId()))
            return Response.status(403).build();

        var collaborator = new WorkspaceCollaboratorEntity(workspace, user);
        collaborator.persist();
        invitation.delete();

        return Response.status(200).build();
    }

    @POST
    @Transactional
    @Authenticated
    @Path("/{workspace}/invitations/{invitation}/decline")
    public Object declineInvitation(@Context SecurityContext context, @PathParam("invitation") Long invitationId, @PathParam("workspace") String workspaceId) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        var invitation = WorkspaceInvitationEntity.<WorkspaceInvitationEntity>findById(invitationId);

        if (invitation == null)
            return Response.status(404).build();

        if (!user.getId().equals(invitation.getUser().getId()))
            return Response.status(403).build();

        invitation.delete();

        return Response.status(200).build();
    }

    @DELETE
    @Transactional
    @Authenticated
    @Path("/{workspace}/invitations/{user}")
    public Object deleteInvitation(@Context SecurityContext context, @PathParam("user") String userId, @PathParam("workspace") String workspaceId) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        var invitation = WorkspaceInvitationEntity.<WorkspaceInvitationEntity>find("user_id = ?1", userId).firstResult();

        if (invitation == null)
            return Response.status(404).build();

        if (!user.getId().equals(workspace.getOwner().getId()))
            return Response.status(403).build();

        invitation.delete();
        return Response.status(200).build();
    }

}
