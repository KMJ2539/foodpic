package com.foodpic.backend.vision;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class StubFoodVisionService implements FoodVisionService {

    private final List<String> labelPool = List.of(
            "apple", "banana", "kimchi", "bibimbap", "salad", "ramen", "sandwich", "unknown_dish"
    );

    private final FoodItemCatalog foodItemCatalog;

    public StubFoodVisionService(FoodItemCatalog foodItemCatalog) {
        this.foodItemCatalog = foodItemCatalog;
    }

    @Override
    public List<FoodCandidate> detectCandidates(String photoAssetId) {
        String normalized = photoAssetId.trim().toLowerCase(Locale.ROOT);
        long hash = Integer.toUnsignedLong(normalized.hashCode());

        return IntStream.range(0, 3)
                .mapToObj(index -> {
                    String label = labelPool.get((int) ((hash + (long) index * 7) % labelPool.size()));
                    int confidenceBucket = (int) ((hash >>> (index * 5)) % 35);
                    double confidence = 0.6 + (confidenceBucket / 100.0);
                    return toCandidate(label, confidence);
                })
                .toList();
    }

    private FoodCandidate toCandidate(String label, double confidence) {
        FoodItem mapped = foodItemCatalog.mapLabel(label);
        if (mapped != null) {
            return new FoodCandidate(label, confidence, mapped, null);
        }

        return new FoodCandidate(label, confidence, null, toTitleCase(label));
    }

    private String toTitleCase(String input) {
        String spaced = input.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
