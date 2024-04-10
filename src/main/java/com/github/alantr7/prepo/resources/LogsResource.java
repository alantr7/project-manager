package com.github.alantr7.prepo.resources;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.github.alantr7.prepo.entity.LogEntryEntity;
import com.github.alantr7.prepo.util.WhereQuery;

import io.quarkus.panache.common.Sort;

import java.util.Arrays;

@Resource
@Path("/api/v1/logs")
public class LogsResource {
    
    @GET
    // TODO: Clean up this mess
    public Object getActivityLogs(@QueryParam("before") Long before, @QueryParam("after") Long after, @QueryParam("action") LogEntryEntity.Action action, @QueryParam("authors") String authorsRaw, @QueryParam("projects") String projectsRaw) {
        if (before == null && after == null && action == null && authorsRaw == null && projectsRaw == null) {
            return LogEntryEntity.listAll(Sort.by("timestamp").descending());
        }

        var queryBuilder = new StringBuilder();

        if (before != null) {
            queryBuilder.append(" AND l.timestamp < :timestampBefore");
        }
        if (after != null) {
            queryBuilder.append(" AND l.timestamp > :timestampAfter");
        }
        if (action != null) {
            queryBuilder.append(" AND l.action = :action");
        }
        if (authorsRaw != null) {
            queryBuilder.append(" AND l.user.id IN (:authors)");
        }
        if (projectsRaw != null) {
            queryBuilder.append(" AND l.project_name IN (:projects)");
        }
        if (queryBuilder.toString().startsWith(" AND "))
            queryBuilder.delete(0, 5);

        var query = LogEntryEntity.getEntityManager().createQuery("SELECT l FROM activity_logs l WHERE " + queryBuilder.toString() + " ORDER BY timestamp DESC", LogEntryEntity.class);
        if (before != null) {
            query.setParameter("timestampBefore", before);
        }
        if (after != null) {
            query.setParameter("timestampAfter", after);
        }
        if (action != null) {
            query.setParameter("action", action);
        }
        if (authorsRaw != null) {
            query.setParameter("authors", Arrays.asList(authorsRaw.contains(";") ? authorsRaw.split(";") : new String[] { authorsRaw }));
        }
        if (projectsRaw != null) {
            query.setParameter("projects", Arrays.asList(projectsRaw.contains(";") ? projectsRaw.split(";") : new String[] { projectsRaw }));
        }

        return query.getResultList();
    }

}
