package com.foodpic.backend.vision;

import java.util.Map;

public class FoodItemCatalog {
    private final Map<String, FoodItem> labelToFoodItem = Map.of(
            "apple", new FoodItem("food-apple", "Apple", 95, 0.5, 25.0, 0.3),
            "banana", new FoodItem("food-banana", "Banana", 105, 1.3, 27.0, 0.4),
            "kimchi", new FoodItem("food-kimchi", "Kimchi", 23, 1.1, 4.0, 0.6),
            "bibimbap", new FoodItem("food-bibimbap", "Bibimbap", 560, 19.0, 86.0, 16.0),
            "salad", new FoodItem("food-salad", "Mixed Salad", 152, 4.1, 10.0, 11.0)
    );

    public FoodItem mapLabel(String label) {
        return labelToFoodItem.get(label.toLowerCase());
    }
}
