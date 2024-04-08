package com.github.alantr7.prepo.resources;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.github.alantr7.prepo.dto.ProjectIssueTaskDTO;
import com.github.alantr7.prepo.dto.board.BoardList2DTO;
import com.github.alantr7.prepo.dto.board.BoardListCard2DTO;
import com.github.alantr7.prepo.entity.*;

import io.quarkus.panache.common.Sort;

@Resource
//@Authenticated
@Path("/api/v1/workspaces/{workspace}/board")
public class BoardResource {

    @PathParam("workspace")
    String workspaceId;

    @GET
    public Object getBoard() {
        var workspace = WorkspaceEntity.<WorkspaceEntity>find("weak_id = ?1", workspaceId).firstResult();
        if (workspace == null)
            return Response.status(404).build();

        var groups = BoardList.find("workspace_id = ?1", workspace.id).project(BoardList2DTO.class).list();
        var groupedCards = ProjectIssueEntity.getEntityManager().createQuery("" +
                "SELECT NEW com.github.alantr7.prepo.dto.board.BoardListCard2DTO(" +
                "bc.list.id, bc.id, iss.id, iss.project.id, iss.title, iss.description, iss.creationDate, iss.state, iss.ind, iss.author" +
                ")" +
                " FROM issues iss LEFT JOIN board_cards bc ON iss.id = bc.issue.id WHERE iss.isResolved = false AND iss.project.workspace.weakId = :workspaceId", BoardListCard2DTO.class
        )
                .setParameter("workspaceId", workspaceId)
                .getResultList().stream().collect(Collectors.groupingBy(obj -> obj.listId));

        var tasksQuery = ProjectIssueTaskEntity.getEntityManager().createQuery(
                "SELECT NEW com.github.alantr7.prepo.dto.ProjectIssueTaskDTO(t.id, t.text, t.creationDate, t.isCompleted, t.issue.id) FROM issue_tasks t", ProjectIssueTaskDTO.class);

        var labels = (List<Object[]>) ProjectIssueLabelEntity.getEntityManager().createNativeQuery(
                "SELECT * FROM issues_labels LEFT JOIN labels ON issues_labels.issue_id = labels.id"
        ).<Object[]>getResultList();

        var attachmentsQuery = ProjectIssueAttachmentEntity.getEntityManager().createQuery("SELECT a FROM issue_attachments a");

        var tasks = tasksQuery.getResultList();
        var attachments = (List<ProjectIssueAttachmentEntity>) attachmentsQuery.getResultList();

        groupedCards.values().forEach(cards -> cards.forEach(card -> {
            var issue = card.issue;
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
        }));

        var groupsMap = new LinkedHashMap<Long, BoardList2DTO>();
        var uncategorized = new BoardList2DTO(0L, "Uncategorized");
        groupsMap.put(0L, uncategorized);
        groups.forEach(group -> groupsMap.put(group.id, group));

        groupedCards.forEach((listId, cards) -> {
            groupsMap.get(listId).cards = cards;
        });

        return groupsMap.values();
    }

    @POST
    @Path("/lists")
    @Transactional
    public Object createList(@NotNull @FormParam("name") String name) {
        var list = new BoardList();
        list.setName(name);

        // TODO
        var workspace = WorkspaceEntity.<WorkspaceEntity>findAll().firstResult();
        list.setWorkspace(workspace);

        list.setPosition(workspace.getNextBoardListPosition());
        workspace.setNextBoardListPosition((int) list.getPosition() + 1000);

        list.persist();
        return list;
    }

    @PUT
    @Path("/lists/{id}")
    @Transactional
    public Object updateList(@PathParam("id") Long listId, @FormParam("name") String name, @FormParam("position") Integer position) {
        var list = BoardList.<BoardList>findById(listId);
        if (list == null) {
            return Response.status(404).build();
        }

        if (listId != null && (long) listId != 0) {
            if (listId == 0) {
                return Response.ok().build();
            }
        }

        if (name != null) {
            list.setName(name);
        }

        if (position != null) {
            var lists = new LinkedList<BoardList>();
            lists.add(BoardList.getDefault());
            lists.addAll(BoardList.<BoardList>listAll(Sort.ascending("position")));

            var results = lists.size() >= position ? lists.subList(position, Math.min(lists.size(), position + 1)).stream().map(BoardList::getPosition).collect(Collectors.toList()) : Collections.emptyList();

            float min, max;

            if (results.size() == 2) {
                min = (float) results.get(0);
                max = (float) results.get(1);
            } else if (results.size() == 1) {
                min = 0f;
                max = (float) results.get(0);
            } else {
                min = max = 0;
            }

            if (results.size() != 0) {
                float realPosition = min + (max - min) / 2;
                list.setPosition(realPosition);
            } else {
                list.setPosition(list.getWorkspace().getNextBoardListPosition());
                list.getWorkspace().setNextBoardListPosition(list.getWorkspace().getNextBoardListPosition() + 1000);
            }

        }

        return Response.ok(list).build();
    }

    @DELETE
    @Path("/lists/{id}")
    @Transactional
    public Object deleteList(@NotNull @PathParam("id") long listId) {
        var list = BoardList.findById(listId);
        if (list == null)
            return Response.status(404).build();

        list.delete();
        return Response.ok().build();
    }

    @PUT
    @Path("/cards/{id}")
    @Transactional
    public Object updateCard(@PathParam("id") long cardId, @NotNull @FormParam("list") Long listId,
                             @FormParam("position") Integer position) {
        var card = BoardListCard.<BoardListCard>find("issue_id = ?1", cardId).firstResult();
        boolean justCreated = card == null;

        if (card == null) {
            var issue = ProjectIssueEntity.<ProjectIssueEntity>findById(cardId);
            if (issue != null && !issue.isResolved()) {
                card = new BoardListCard(issue);
            } else {
                return Response.status(404).build();
            }

            justCreated = true;
        }

        if (listId != null && (long) listId != (long) (card.getList() != null ? card.getList().id : 0)) {
            if (listId == 0) {
                card.delete();
                return Response.ok().build();
            }

            var list = BoardList.<BoardList>findById(listId);
            if (list == null)
                return Response.status(404).build();

            if (card.getList() != null) {
                card.getList().getCards().removeIf(card1 -> (long) card1.id == (long) cardId);
            }
            card.setList(list);
            list.getCards().add(card);

            if (position == null) {
                card.setPosition(list.getNextCardPosition());
                list.setNextCardPosition(list.getNextCardPosition() + 1000);
            }
        }

        float min = -1;
        float max = -1;

        List results0 = null;

        if (position != null) {
            // var query = BoardList.getEntityManager().createNativeQuery(
            // "select position from board_cards where list_id = :list_id order by position asc limit :index , 2"
            // );
// 
            // query.setParameter("index", position);
            // query.setParameter("list_id", listId);

            // var results = query.getResultList();

            var cards = card.getList().getCards();
            var results = cards.size() >= position ? cards.subList(position, Math.min(cards.size(), position + 1)).stream().map(BoardListCard::getPosition).collect(Collectors.toList()) : Collections.emptyList();

            results0 = results;

            if (results.size() == 2) {
                min = (float) results.get(0);
                max = (float) results.get(1);
            } else if (results.size() == 1) {
                min = 0f;
                max = (float) results.get(0);
            } else {
                min = max = 0;
            }

            if (results.size() != 0) {
                float realPosition = min + (max - min) / 2;
                card.setPosition(realPosition);
            } else {
                card.setPosition(card.getList().getNextCardPosition());
            }

        }

        if (justCreated)
            card.persist();

        return Response.ok(card).entity(card.getList().id + ", " + card.getPosition() + ", minmax: " + min + ", " + max + ", results: " + results0.size()).build();
    }

}
