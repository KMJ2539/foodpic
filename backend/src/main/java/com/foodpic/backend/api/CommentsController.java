package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.CommentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/comments")
public class CommentsController {
    @GetMapping("/{commentId}")
    public CommentResponse get(@PathVariable String commentId) {
        return new CommentResponse(commentId, "post-1", "demo-user", "great", Instant.parse("2024-01-01T00:00:00Z"));
    }
}
