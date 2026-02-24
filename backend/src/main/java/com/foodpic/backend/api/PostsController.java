package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.PostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/posts")
public class PostsController {
    @GetMapping("/{postId}")
    public PostResponse get(@PathVariable String postId) {
        return new PostResponse(postId, "demo-user", "sample post", Instant.parse("2024-01-01T00:00:00Z"));
    }
}
