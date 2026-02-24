package com.foodpic.backend.vision;

public record FoodItem(
        String id,
        String canonicalName,
        Integer caloriesKcal,
        Double proteinGram,
        Double carbGram,
        Double fatGram
) {
}
