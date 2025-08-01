package com.pray.board.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pray.board.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    
    private Long id;
    private Long boardId;
    private Long parentId;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    private List<CommentResponseDto> children = new ArrayList<>();

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.boardId = comment.getBoard().getId();
        this.parentId = (comment.getParent() != null ? comment.getParent().getId() : null);
        this.content = comment.getContent();
        this.author = comment.getAuthor();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.deleted = (comment.getDeletedAt() != null);
    }

    public boolean isDeleted() {
        return deleted;
    }
}
