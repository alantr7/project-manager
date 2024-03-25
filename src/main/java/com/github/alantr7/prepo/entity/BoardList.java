package com.github.alantr7.prepo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.github.alantr7.prepo.util.ListCriteriaUtil;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "board_lists")
public class BoardList extends PanacheEntity {
    
    String name;

    @OneToMany(mappedBy = "list", fetch = FetchType.EAGER)
    @OrderBy("position asc")
    List<BoardListCard> cards = Collections.emptyList();

    String criteria = "";

    float position = 1000;

    int nextCardPosition = 1000;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    WorkspaceEntity workspace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public List<BoardListCard> getCards() {
        return cards != null ? cards : Collections.emptyList();
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

    public int getNextCardPosition() {
        return nextCardPosition;
    }

    public void setNextCardPosition(int nextPosition) {
        this.nextCardPosition = nextPosition;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    static Collection<BoardList> getApplicableLists(ProjectIssueEntity entity) {
        return Collections.emptyList();
    }

    static Collection<BoardListCard> getCardsBasedOnCriteria(BoardList list, String criteria) {
        var values = ListCriteriaUtil.getValues(criteria);
        if (values == null) return Collections.emptyList();

        List<ProjectIssueEntity> issues = new ArrayList<>();

        String label = values.get("label");
        if (label != null) {
            var query = list.getEntityManager().createNativeQuery("select * from issues issue join issues_labels l on issue.id = l.label_id where l.issue_id = :issue_id", ProjectIssueEntity.class);
            query.setParameter("issue_id", label);

            issues.addAll((List<ProjectIssueEntity>) query.getResultList());
        }

        

        String closedRaw = values.get("closed");
        if (closedRaw != null && (closedRaw.equals("true") || closedRaw.equals("false"))) {
            boolean closed = Boolean.parseBoolean(closedRaw);
            if (closed) {
                if (issues.size() == 0) {
                    issues.addAll(ProjectIssueEntity.find("isResolved = ?1", closed).list());
                } else {
                    issues.removeIf(issue -> issue.isResolved == closed);
                }
            }
        }

        // query.setParameter("issue_id", "api");

        return issues.stream().map(issue -> {
            var card = new BoardListCard();
            card.id = 0L;
            card.issue = issue;
            card.list = list;

            return card;
        }).collect(Collectors.toList());
    }

    public static BoardList getDefault() {
        var query = getEntityManager().createNativeQuery("select * from issues iss where not iss.`isResolved` and iss.id not in (select issue_id from board_cards)", ProjectIssueEntity.class);
        var list = new BoardList();
        list.id = 0L;
        list.name = "Uncategorized";
        list.cards = new LinkedList<>();

        query.getResultList().forEach(item -> {
            var card = new BoardListCard((ProjectIssueEntity) item);
            card.id = ((ProjectIssueEntity) item).id;

            list.cards.add(card);
        });;

        return list;
    }

}
