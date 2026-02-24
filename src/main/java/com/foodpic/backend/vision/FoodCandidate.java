package com.foodpic.backend.vision;

public record FoodCandidate(
        String label,
        double confidence,
        FoodItem foodItem,
        String freeTextName
) {
}
