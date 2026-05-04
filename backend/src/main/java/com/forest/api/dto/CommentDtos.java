package com.forest.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class CommentDtos {
    public record CommentRequest(@NotBlank String content) {}

    public record CommentResponse(Long id, String content, Long postId, Long authorId, String authorUsername, Instant createdAt) {}
}
