package com.github.alantr7.prepo.resources;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.github.alantr7.prepo.entity.LogEntryEntity;
import com.github.alantr7.prepo.util.WhereQuery;

import io.quarkus.panache.common.Sort;

@Resource
@Path("/api/v1/logs")
public class LogsResource {
    
    @GET
    public Object getActivityLogs(@QueryParam("before") Long before, @QueryParam("after") Long after, @QueryParam("action") LogEntryEntity.Action action) {
        if (before == null && after == null && action == null) {
            return LogEntryEntity.listAll(Sort.by("timestamp").descending());
        }

        var query = new WhereQuery();
        
        if (before != null) {
            query.put("timestamp < %n", before);
        }
        if (after != null) {
            query.put("timestamp > %n", after);
        }
        if (action != null) {
            query.put("action = %n", action);
        }

        return LogEntryEntity.find(query.getQuery(), Sort.by("timestamp").descending(), query.getParameters()).list();
    }

}
