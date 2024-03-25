package com.github.alantr7.prepo.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.github.alantr7.prepo.entity.BoardList;

public class BoardListDTO {
    
    public String name;

    public long id;

    public List<BoardListCardDTO> cards;

    public BoardListDTO(BoardList list) {
        id = list.id;
        name = list.getName();
        cards = list.getCards().stream().map(card -> new BoardListCardDTO(card)).collect(Collectors.toList());
    }

}
