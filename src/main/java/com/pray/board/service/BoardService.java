package com.pray.board.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pray.board.dto.BoardResponseDto;
import com.pray.board.entity.Board;
import com.pray.board.entity.BoardLikes;
import com.pray.board.repository.BoardLikesRepository;
import com.pray.board.repository.BoardRepository;

import jakarta.persistence.EntityNotFoundException;



@Service
@Transactional( readOnly = true )
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardLikesRepository boardLikesRepository;

    public BoardService(BoardRepository boardRepository, BoardLikesRepository boardLikesRepository) {
        this.boardRepository = boardRepository;
        this.boardLikesRepository = boardLikesRepository;
    }
    
    public Page<BoardResponseDto> getAllBoards(Pageable pageable) {
        Page<Board> boardPage = boardRepository.findAll(pageable);

        return boardPage.map(board -> {
            long likeCount = boardLikesRepository.countByBoardIdAndIsLike(board.getId(), true);
            long dislikeCount = boardLikesRepository.countByBoardIdAndIsLike(board.getId(), false);
            return new BoardResponseDto(board, likeCount, dislikeCount);
        });
    }

    public BoardResponseDto getBoardById(Long id) {
        Board board = boardRepository.findById(id) 
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));

        long likeCount = boardLikesRepository.countByBoardIdAndIsLike(id, true);
        long dislikeCount = boardLikesRepository.countByBoardIdAndIsLike(id, false);

        return new BoardResponseDto(board, likeCount, dislikeCount);
    }

    public Board getBoardEntityById(Long id) {
        return boardRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    @Transactional
    public void toggleLike(Long boardId, String author, boolean isLike) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + boardId));

        Optional<BoardLikes> existingLike = boardLikesRepository.findByBoardIdAndAuthor(boardId, author);

        if (existingLike.isPresent()) {
            BoardLikes like = existingLike.get();
            if (like.getIsLike() == isLike) {
                boardLikesRepository.delete(like);
            } else {
                like.setIsLike(isLike);
                boardLikesRepository.save(like);
            }
        } else {
            BoardLikes newLike = BoardLikes.builder()
                    .board(board)
                    .author(author)
                    .isLike(isLike)
                    .build();
            boardLikesRepository.save(newLike);
        }
    }
}
