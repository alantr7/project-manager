package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "board_cards")
public class BoardListCard extends PanacheEntity {
    
    @ManyToOne
    @JoinColumn(name = "list_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    BoardList list;

    @ManyToOne
    @JoinColumn(name = "issue_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    ProjectIssueEntity issue;

    float position;

    public BoardListCard() {
    }

    public BoardListCard(ProjectIssueEntity issue) {
        this.issue = issue;
    }

    public ProjectIssueEntity getIssue() {
        return issue;
    }

    @JsonIgnore
    public BoardList getList() {
        return list;
    }

    public void setList(BoardList list) {
        this.list = list;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

}
