package com.github.alantr7.prepo.dto.board;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.LinkedList;
import java.util.List;

@RegisterForReflection
public class BoardList2DTO {

    public long id;

    public String name;

    public List<BoardListCard2DTO> cards = new LinkedList<>();

    public BoardList2DTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
