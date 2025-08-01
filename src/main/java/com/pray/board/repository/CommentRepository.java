package com.pray.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pray.board.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 1. 'board' 엔티티 내부의 'id'를 명확히 지정합니다: findByBoard_Id
    List<Comment> findByBoardIdAndParentIsNullOrderByCreatedAtAsc(Long boardId);

    // 2. 이 메서드는 boardId를 직접 참조하지 않고 board 엔티티의 id를 참조하므로 변경하지 않습니다.
    // 하지만 일관성을 위해 findByBoard_IdOrderByCreatedAtAsc 로 변경하는 것도 좋습니다.
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId); // <-- 여기도 변경하는 게 안전합니다.
}