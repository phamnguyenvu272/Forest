package com.forest.api.service;

import com.forest.api.dto.CommentDtos.CommentRequest;
import com.forest.api.dto.CommentDtos.CommentResponse;
import com.forest.api.model.Comment;
import com.forest.api.model.Post;
import com.forest.api.model.User;
import com.forest.api.repository.CommentRepository;
import com.forest.api.repository.PostRepository;
import com.forest.api.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> getByPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream().map(this::toResponse).toList();
    }

    public CommentResponse addToPost(Long postId, CommentRequest request, String email) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Post not found"));
        User author = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setPost(post);
        comment.setAuthor(author);
        return toResponse(commentRepository.save(comment));
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getContent(), c.getPost().getId(), c.getAuthor().getId(), c.getAuthor().getUsername(), c.getCreatedAt());
    }
}
