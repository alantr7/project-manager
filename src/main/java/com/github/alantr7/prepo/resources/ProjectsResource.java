package com.github.alantr7.prepo.resources;

import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectFileEntity;
import com.github.alantr7.prepo.entity.ProjectGroupEntity;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;

@Resource
@Path("/api/v1/projects")
public class ProjectsResource {

    @GET @Transactional
    @Path("/{project}")
    public Object getProject(@PathParam("project") String projectId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        return project;
    }

    @PUT @Transactional
    @Path("/{project}")
    public Object updateProject(@PathParam("project") String projectId, @FormParam("group") Long groupId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        if (project == null) {
            return Response.status(404).build();
        }

        if (groupId != null) {
            project.setGroup(groupId == 0 ? null : ProjectGroupEntity.<ProjectGroupEntity>findById(groupId));
        }

        return project;
    }

    @DELETE @Transactional
    @Path("/{project}")
    public Object deleteProject(@PathParam("project") String projectId) {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        if (project == null)
            return Response.status(404).build();

        project.delete();

        return Response.status(200).build();
    }

    @POST @Path("/{project}/files")
    @Authenticated
    @Transactional
    public Object uploadFile(@PathParam("project") String projectId, @FormParam("name") String fileName, @NotNull @FormParam("file") @PartType(MediaType.APPLICATION_OCTET_STREAM) FileUpload upload) throws Exception {
        var project = ProjectEntity.<ProjectEntity>findById(projectId);
        var file = project.createFile();

        var destination = new File(System.getProperty("user.dir"), "files");
        destination.mkdirs();

        file.setName(fileName);
        file.setSize(upload.size());

        var binary = new File(destination, file.getId());
        Files.copy(upload.uploadedFile(), binary.toPath());

        return file;
    }

    @GET @Path("/{project}/files/{id}")
    public Object getFile(@PathParam("id") String fileId) {
        var file = ProjectFileEntity.<ProjectFileEntity>findById(fileId);
        if (file == null)
            return Response.status(404).build();

        return file;
    }

    // TODO: Check if entity tag has changed (etag, hashing, etc.)
    @GET @Path("/{project}/files/{id}/preview")
    // @Produces({ "image/jpeg", "image/gif", "image/png" })
    @Transactional
    public Response previewFile(@PathParam("id") String fileId) throws Exception {
        var file = ProjectFileEntity.<ProjectFileEntity>findById(fileId);
        if (file == null)
            return Response.status(404).build();

        var name = file.getName();
        var extension = name.toLowerCase().substring(name.lastIndexOf('.') + 1);

        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif"))
            return Response.status(400).build();

        var binary = file.getBinary();

        Response.ResponseBuilder response = Response.ok((Object) binary);
        response.type("image/png");
        response.header("Content-Disposition", "inline;filename=\"" + name + "\"");
        return response.build();
    }

    @GET @Path("/{project}/files/{id}/download")
    @Transactional
    public Response downloadFile(@PathParam("id") String fileId) throws Exception {
        var file = ProjectFileEntity.<ProjectFileEntity>findById(fileId);
        if (file == null)
            throw new NotFoundException("File not found");

        var binary = file.getBinary();
        var name = file.getName();

        Response.ResponseBuilder response = Response.ok((Object) binary);
        response.type(MediaType.APPLICATION_OCTET_STREAM);
        response.header("Content-Disposition", "attachment;filename=\"" + name + "\"");
        // return Uni.createFrom().item(response.build());

        return response.build();
    }

    @DELETE @Path("/{project}/files/{id}")
    @Transactional
    public Object deleteFile(@PathParam("project") String projectId, @PathParam("id") String fileId) {
        var file = ProjectFileEntity.<ProjectFileEntity>findById(fileId);
        if (file == null) return Response.status(404).entity("file_not_found").build();

        file.delete();
        file.getBinary().delete();
        return Response.ok().build();
    }

}