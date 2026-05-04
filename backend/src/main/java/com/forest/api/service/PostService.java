package com.forest.api.service;

import com.forest.api.dto.PostDtos.PostRequest;
import com.forest.api.dto.PostDtos.PostResponse;
import com.forest.api.model.Post;
import com.forest.api.model.User;
import com.forest.api.repository.PostRepository;
import com.forest.api.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<PostResponse> getFeed() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public PostResponse getById(Long id) {
        return toResponse(postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Post not found")));
    }

    public PostResponse create(PostRequest request, String email) {
        User author = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setAuthor(author);
        return toResponse(postRepository.save(post));
    }

    public PostResponse update(Long id, PostRequest request, String email) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Post not found"));
        if (!post.getAuthor().getEmail().equals(email)) throw new ResponseStatusException(FORBIDDEN, "Only author can update this post");
        post.setTitle(request.title());
        post.setContent(request.content());
        return toResponse(postRepository.save(post));
    }

    public void delete(Long id, String email) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Post not found"));
        if (!post.getAuthor().getEmail().equals(email)) throw new ResponseStatusException(FORBIDDEN, "Only author can delete this post");
        postRepository.delete(post);
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getId(), post.getAuthor().getUsername(), post.getCreatedAt());
    }
}
