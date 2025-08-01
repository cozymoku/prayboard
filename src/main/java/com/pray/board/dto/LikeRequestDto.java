package com.pray.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequestDto {
    private String author;
    private Boolean isLike;
}
