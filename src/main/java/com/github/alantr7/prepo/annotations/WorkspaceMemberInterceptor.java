package com.github.alantr7.prepo.annotations;

import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.entity.WorkspaceEntity;
import io.quarkus.security.runtime.interceptor.AuthenticatedInterceptor;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import javax.transaction.Transactional;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@WorkspaceMember
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class WorkspaceMemberInterceptor {

    @Inject
    SecurityContext context;

    @Context
    UriInfo uriInfo;

    Map<Method, String> pathCache = new HashMap<>();

    @AroundInvoke
    @Transactional
    public Object checkWorkspaceMembership(InvocationContext ctx) throws Exception {
        if (context.getUserPrincipal() == null)
            return Response.status(401).build();

        var annotation = ctx.getMethod().getAnnotation(WorkspaceMember.class);
        var workspaceId = uriInfo.getPathParameters().getFirst(annotation.workspaceParam());

        var user = UserEntity.<UserEntity>findById(context.getUserPrincipal().getName());
        int results = WorkspaceEntity.getEntityManager().createQuery(
                        "select w from workspaces w " +
                                "left join workspaces_collaborators wc on wc.workspace.id = w.id " +
                                "where w.weakId = :workspaceId and (w.owner.id = :userId or wc.user.id = :userId)"
                )
                .setParameter("userId", user.getId())
                .setParameter("workspaceId", workspaceId)
                .getResultList().size();

        if (results == 0) {
            return Response.status(403).build();
        }

        return ctx.proceed();
    }


}
