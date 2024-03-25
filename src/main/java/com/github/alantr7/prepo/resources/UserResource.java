package com.github.alantr7.prepo.resources;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.ConfigProperty;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.alantrumic.ednevnik.entity.*;
import com.github.alantr7.prepo.dto.NotificationsDTO;
import com.github.alantr7.prepo.entity.UserAvatarEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.entity.UserNotificationEntity;
import com.github.alantr7.prepo.entity.WorkspaceInvitationEntity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.github.alantr7.prepo.entity.UserNotificationEntity.Type;

import io.quarkus.security.Authenticated;
import io.vertx.core.http.HttpServerResponse;
// import org.eclipse.microprofile.jwt.JsonWebToken;;

@Resource
@Path("/api/v1/users")
public class UserResource {

    @ConfigProperty
    String cookieName = "quarkus.http.auth.form.cookie-name";

    // @IdToken
    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/me")
    @Authenticated
    public Object me(@Context SecurityContext context) {
        var principal = context.getUserPrincipal();
        var name = principal.getName();
        var user = UserEntity.findById(name);

        return user;
    }

    @GET
    @Path("/me/invitations")
    @Authenticated
    public Object getInvitations(@Context SecurityContext context) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var invitations = WorkspaceInvitationEntity.find("user_id = ?1", user.getId()).list();

        return invitations;
    }

    @GET
    @Path("/me/notifications")
    @Authenticated
    public Object getNotifications(@Context SecurityContext context) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());

        var query = UserNotificationEntity.getEntityManager().createNativeQuery("select * from notifications notifs join notifications_recipients recips where recips.notification_id = notifs.id and recips.recipient_id = :recipient order by notifs.date desc", UserNotificationEntity.class);
        query.setParameter("recipient", context.getUserPrincipal().getName());

        var notifications = (Collection<UserNotificationEntity>) query.getResultList();
        int unseen = 0;

        for (var notif : notifications) {
            if (user.getLastSeenNotification() < notif.id)
                unseen++;
        }

        return new NotificationsDTO(notifications, user.getLastSeenNotification(), unseen);
    }

    @POST
    @Path("/me/notifications/seen")
    @Transactional
    @Authenticated
    public Object seenNotifications(@Context SecurityContext context, @FormParam("notification") @NotNull Long id) {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var notification = UserNotificationEntity.findById(id);

        if (notification == null) return Response.status(404).build();

        user.setLastSeenNotification(id);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Authenticated
    @Path("/me/notifications")
    public Object test() {
        var notification = UserNotificationEntity.create(Type.TEXT, "Test", Collections.singletonList(UserEntity.findById("JPgxfHHfCJQh0iCwr0QMpsc8MDu2")));
        notification.persist();

        return notification;
    }

    @POST
    @Path("/me/avatar")
    @Authenticated
    @Transactional
    public Object updateAvatar(@Context SecurityContext context, @NotNull @FormParam("file") @PartType(MediaType.APPLICATION_OCTET_STREAM) FileUpload upload) throws Exception {
        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        var destination = new File(System.getProperty("user.dir"), "avatars");
        destination.mkdirs();

        var file = UserAvatarEntity.create(user);

        var name = upload.fileName();
        var extension = name.toLowerCase().substring(name.lastIndexOf('.') + 1);

        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif") && !extension.equals("webp"))
            return Response.status(400).build();

        file.setName(name);
        file.setSize(upload.size());

        var binary = new File(destination, file.getId());
        Files.copy(upload.uploadedFile(), binary.toPath());

        var previous = UserAvatarEntity.<UserAvatarEntity>find("user = ?1", user).firstResult();
        if (previous != null) {
            UserAvatarEntity.deleteById(previous.getId());
        }

        file.persist();
        return file;
    }

    @GET
    @Path("/me/logout")
    public Object logout(HttpServerResponse context) {
        context.removeCookie("quarkus-credential");
        return context;
    }

    @GET
    @Path("/{user}")
    public Object getUser(@PathParam("user") String userId) {
        var user = UserEntity.<UserEntity>findById(userId);
        if (user == null)
            return Response.status(404).build();

        return Response.ok(user).build();
    }

}
