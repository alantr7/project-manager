package com.github.alantr7.prepo.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectGroupEntity;
import com.github.alantr7.prepo.entity.WorkspaceEntity;

@Resource @Path("/api/v1/workspaces/{workspace}/groups")
public class GroupsResource {

    @PathParam("workspace")
    String workspaceId;
    
    @GET
    public Collection<Object> getGroups() {
        List<Object> groups = new ArrayList<>(ProjectGroupEntity.<ProjectGroupEntity>listAll());
        groups.add(ProjectGroupEntity.DEFAULT);
        
        return groups;
    }

    @POST @Transactional
    public Object createGroup(@FormParam("name") @NotNull String name) {
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        if (workspace == null)
            return Response.status(404).build();

        var group = new ProjectGroupEntity();
        group.setName(name);
        group.setWorkspace(workspace);
//        group.setWorkspace();

        group.persist();
        return group;
    }

    @DELETE @Transactional
    @Path("/{id}")
    public Object deleteGroup(@PathParam("id") long id) {
        if (id == 0) {
            return Response.status(400).entity("can not delete the default group").build();
        }

        var group = ProjectGroupEntity.<ProjectGroupEntity>findById(id);
        if (group == null)
            return Response.status(404).build();
        
        
        ProjectEntity.update("set group_id = null where group_id = ?1", id);
        group.delete();

        return Response.ok().build();
    }

}
