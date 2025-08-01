package com.pray.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pray.board.entity.BoardLikes;

@Repository
public interface BoardLikesRepository extends JpaRepository<BoardLikes, Long> {

    Optional<BoardLikes> findByBoardIdAndAuthor(Long boardId, String author);
    
    @Query("SELECT COUNT(l) FROM BoardLikes l WHERE l.board.id = :boardId AND l.isLike = :isLike")
    long countByBoardIdAndIsLike(@Param("boardId") Long boardId, @Param("isLike") boolean isLike);
}
