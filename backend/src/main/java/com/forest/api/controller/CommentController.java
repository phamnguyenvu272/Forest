package com.forest.api.controller;

import com.forest.api.dto.CommentDtos.CommentRequest;
import com.forest.api.dto.CommentDtos.CommentResponse;
import com.forest.api.service.CommentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) { this.commentService = commentService; }

    @GetMapping
    public List<CommentResponse> list(@PathVariable Long postId) { return commentService.getByPost(postId); }

    @PostMapping
    public CommentResponse create(@PathVariable Long postId, @Valid @RequestBody CommentRequest request, Principal principal) {
        return commentService.addToPost(postId, request, principal.getName());
    }
}
