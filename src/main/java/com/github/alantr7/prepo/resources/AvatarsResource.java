package com.github.alantr7.prepo.resources;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.github.alantr7.prepo.entity.UserAvatarEntity;

@Resource @Path("/api/v1/avatars")
public class AvatarsResource {
    
    @GET @Path("/{id}")
    @Transactional
    public Response getAvatar(@PathParam("id") String fileId) throws Exception {
        var file = fileId.equals("default.png") ? UserAvatarEntity.DEFAULT_AVATAR : UserAvatarEntity.<UserAvatarEntity>findById(fileId);
        if (file == null)
            return Response.status(404).build();

        var name = file.getName();
        var extension = name.toLowerCase().substring(name.lastIndexOf('.') + 1);

        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif") && !extension.equals("webp"))
            return Response.status(400).build();

        var binary = file.getBinary();

        Response.ResponseBuilder response = Response.ok((Object) binary);
        response.type("image/png");
        response.header("Content-Disposition", "inline;filename=\"" + name + "\"");
        return response.build();
    }

}
