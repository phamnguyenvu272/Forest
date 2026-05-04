package com.forest.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class PostDtos {
    public record PostRequest(@NotBlank @Size(min = 3, max = 150) String title, @NotBlank String content) {}

    public record PostResponse(Long id, String title, String content, Long authorId, String authorUsername, Instant createdAt) {}
}
