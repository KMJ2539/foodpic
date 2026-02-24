package com.foodpic.backend.vision;

public record ConfirmedFood(
        String photoAssetId,
        String candidateLabel,
        FoodItem foodItem,
        String freeTextName
) {
}
