package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.FeedItemResponse;
import com.foodpic.backend.api.dto.ResourceDtos.FeedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/v1/feed")
public class FeedController {
    @GetMapping
    public FeedResponse list() {
        return new FeedResponse(List.of(new FeedItemResponse("feed-1", "demo-user", "healthy bowl", Instant.parse("2024-01-01T00:00:00Z"))), null);
    }
}
