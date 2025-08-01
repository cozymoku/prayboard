package com.pray.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.pray.board.entity.Board;
import com.pray.board.service.BoardService;



@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public ResponseEntity<Page<BoardResponseDto>> getAllBoards(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Board> boardPage = boardService.getAllBoard(pageable);
        Page<BoardResponseDto> dtoPage = boardPage.map(BoardResponseDto::new);
        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")    
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable("id") Long id) {
        return boardService.getBoardById(id)
                .map(BoardResponseDto::new)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
        return boardService.getBoardById(id)
                .map(existingBoard -> {
                    existingBoard.setTitle(boardRequestDto.getTitle());
                    existingBoard.setContent(boardRequestDto.getContent());
                    existingBoard.setAuthor(boardRequestDto.getAuthor());

                    Board updatedBoard = boardService.saveBoard(existingBoard);
                    return new ResponseEntity<>(new BoardResponseDto(updatedBoard), HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        if(boardService.getBoardById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        boardService.deleteBoard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
