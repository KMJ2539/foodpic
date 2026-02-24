package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.LikeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/likes")
public class LikesController {
    @GetMapping("/{postId}")
    public LikeResponse get(@PathVariable String postId) {
        return new LikeResponse(postId, 10, true);
    }
}
