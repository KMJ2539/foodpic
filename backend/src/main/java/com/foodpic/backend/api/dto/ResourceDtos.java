package com.foodpic.backend.api.dto;

import java.time.Instant;
import java.util.List;

public class ResourceDtos {
    public record SocialProfileResponse(String userId, String nickname, String bio) {}
    public record FeedItemResponse(String feedId, String authorId, String caption, Instant createdAt) {}
    public record FeedResponse(List<FeedItemResponse> items, String nextCursor) {}
    public record PostResponse(String postId, String authorId, String caption, Instant createdAt) {}
    public record LikeResponse(String postId, int likeCount, boolean liked) {}
    public record CommentResponse(String commentId, String postId, String authorId, String content, Instant createdAt) {}
    public record MealResponse(String mealId, String postId, String mealType, int calorie) {}
    public record PhotoResponse(String photoId, String postId, String imageUrl) {}
    public record VisionResponse(String photoId, List<String> tags, double confidence) {}
}
