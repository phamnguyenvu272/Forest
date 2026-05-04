package com.forest.api.controller;

import com.forest.api.dto.PostDtos.PostRequest;
import com.forest.api.dto.PostDtos.PostResponse;
import com.forest.api.service.PostService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:4200")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) { this.postService = postService; }

    @GetMapping
    public List<PostResponse> feed() { return postService.getFeed(); }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable Long id) { return postService.getById(id); }

    @PostMapping
    public PostResponse create(@Valid @RequestBody PostRequest request, Principal principal) {
        return postService.create(request, principal.getName());
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest request, Principal principal) {
        return postService.update(id, request, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) { postService.delete(id, principal.getName()); }
}
