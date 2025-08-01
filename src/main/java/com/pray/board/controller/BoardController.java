package com.pray.board.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pray.board.dto.BoardRequestDto;
import com.pray.board.dto.BoardResponseDto;
import com.pray.board.dto.LikeRequestDto;
import com.pray.board.entity.Board;
import com.pray.board.service.BoardService;
import com.pray.board.service.CommentService;



@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    public BoardController(BoardService boardService, CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<BoardResponseDto>> getAllBoards(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BoardResponseDto> dtoPage = boardService.getAllBoards(pageable);
        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")    
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable("id") Long id) {
        BoardResponseDto dto = boardService.getBoardById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto boardRequestDto) {
        Board board = Board.builder()
                            .title(boardRequestDto.getTitle())
                            .content(boardRequestDto.getContent())
                            .author(boardRequestDto.getAuthor())
                            .build();        

        Board savedBoard = boardService.saveBoard(board);
        return new ResponseEntity<>(new BoardResponseDto(savedBoard), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable("id") Long id, @RequestBody BoardRequestDto boardRequestDto) {
        Board existingBoard = boardService.getBoardEntityById(id);

        existingBoard.setTitle(boardRequestDto.getTitle());
        existingBoard.setContent(boardRequestDto.getContent());
        existingBoard.setAuthor(boardRequestDto.getAuthor());

        Board updatedBoard = boardService.saveBoard(existingBoard);
        return new ResponseEntity<>(new BoardResponseDto(updatedBoard), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{boardId}/likes")
    public ResponseEntity<Void> toggleLike(@PathVariable Long boardId, @RequestBody LikeRequestDto requestDto) {
        boardService.toggleLike(boardId, requestDto.getAuthor(), requestDto.getIsLike());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
