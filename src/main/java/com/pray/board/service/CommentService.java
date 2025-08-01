package com.pray.board.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pray.board.dto.CommentRequestDto;
import com.pray.board.dto.CommentResponseDto;
import com.pray.board.entity.Board;
import com.pray.board.entity.Comment;
import com.pray.board.repository.BoardRepository;
import com.pray.board.repository.CommentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
    }

    public List<CommentResponseDto> getCommentsByBoardId(Long boardId) {
        // 1. 해당 게시글의 모든 댓글을 created_at 순서로 가져옵니다.
        // Comment 엔티티 내 board 필드의 id를 참조하기 위해 findByBoard_Id를 사용합니다.
        List<Comment> comments = commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);

        // 2. Map을 사용하여 ID를 키로 CommentResponseDto 객체를 매핑합니다.
        // 이렇게 하면 부모 댓글을 빠르게 찾을 수 있습니다.
        Map<Long, CommentResponseDto> commentDtoMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, CommentResponseDto::new));

        // 3. 계층 구조를 만듭니다.
        List<CommentResponseDto> rootComments = new ArrayList<>(); // 최상위 댓글들을 담을 리스트

        commentDtoMap.values().forEach(commentDto -> {
            if (commentDto.getParentId() == null) {
                // parentId가 null이면 최상위 댓글이므로 rootComments 리스트에 추가합니다.
                rootComments.add(commentDto);
            } else {
                // parentId가 있으면 대댓글이므로, 해당 parentId를 가진 부모 댓글을 Map에서 찾습니다.
                CommentResponseDto parentDto = commentDtoMap.get(commentDto.getParentId());
                if (parentDto != null) {
                    // 부모 댓글이 존재하면, 부모 댓글의 children 리스트에 현재 대댓글을 추가합니다.
                    parentDto.getChildren().add(commentDto);
                }
                // 참고: 만약 parentDto가 null인 경우는 부모 댓글이 존재하지 않거나,
                // 어떤 이유로 Map에 포함되지 않은 경우입니다. 이 경우 해당 대댓글은 결과에 포함되지 않습니다.
                // (예: 부모 댓글이 물리적으로 삭제되어 참조가 끊어진 경우 등, 정책에 따라 처리 방식이 달라질 수 있습니다.)
            }
        });

        // 4. 각 댓글의 자식 댓글들(대댓글)을 생성 시간 기준으로 정렬합니다.
        // 이렇게 해야 대댓글이 생성된 순서대로 표시됩니다.
        rootComments.forEach(this::sortChildren);

        return rootComments; // 계층 구조가 완성된 최상위 댓글 리스트를 반환합니다.
    }

    private void sortChildren(CommentResponseDto commentResponseDto) {
        commentResponseDto.getChildren().sort(Comparator.comparing(CommentResponseDto::getCreatedAt));
        commentResponseDto.getChildren().forEach(this::sortChildren);
    }

    @Transactional
    public CommentResponseDto createComment(Long boardId, CommentRequestDto requestDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + boardId));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .author(requestDto.getAuthor())
                .board(board)
                .build();

        // --- 여기에 부모 댓글 상태 검증 로직 추가 ---
        if (requestDto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다: " + requestDto.getParentId()));

            // 부모 댓글이 삭제된 상태인지 확인
            //if (parentComment.getDeletedAt() != null) {
            //    // 삭제된 댓글에는 대댓글을 달 수 없다는 예외를 발생시킵니다.
            //    throw new IllegalArgumentException("삭제된 댓글에는 대댓글을 달 수 없습니다.");
            //    // 또는 throw new IllegalStateException("삭제된 댓글에는 대댓글을 달 수 없습니다.");
            //}

            comment.setParent(parentComment);
            // 양방향 관계 설정 (Comment 엔티티에 addChildComment 메서드가 있다고 가정)
            parentComment.addChildComment(comment);
        }
        // --- 검증 로직 추가 끝 ---

        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto) {
        Comment existingComment = commentRepository.findById(commentId)
                                    .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: "+ commentId));

        if(existingComment.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }

        existingComment.setContent(commentRequestDto.getContent());
        existingComment.setAuthor(commentRequestDto.getAuthor());

        Comment updatedComment = commentRepository.save(existingComment);
        return new CommentResponseDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                            .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + commentId));

        // 소프트삭제, 자식도 삭제를 원하면 CascadeType.ALL 또는 orphanRemoval = true 셋팅
        comment.markAsDeleted();
        commentRepository.save(comment);
    }
}
