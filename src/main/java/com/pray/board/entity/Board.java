package com.pray.board.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Board {
    
    private Long id;

    private String titile;

    private String content;

    private String author;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
