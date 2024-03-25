package com.github.alantr7.prepo.dto;

import com.github.alantr7.prepo.entity.BoardListCard;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;

@Deprecated
// TODO: Delete the class, and add list_id and position to the issue itself
public class BoardListCardDTO {
    
    public long id;
    
    public ProjectIssueEntity issue;

    public float position;

    public BoardListCardDTO(BoardListCard card) {
        issue = card.getIssue();
        id = issue.id;
        position = card.getPosition();
    }

}
