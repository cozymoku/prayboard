package com.pray.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글 내용은 최대 1000자입니다.")
    private String content;
    
    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 255, message = "작성자는 최대 255자입니다.")
    private String author;

    private Long parentId;
}
