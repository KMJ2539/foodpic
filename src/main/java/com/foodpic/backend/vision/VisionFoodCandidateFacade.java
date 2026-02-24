package com.foodpic.backend.vision;

import java.util.List;
import java.util.Locale;

public class VisionFoodCandidateFacade {
    private final FoodVisionService foodVisionService;
    private final FoodItemCatalog foodItemCatalog;

    public VisionFoodCandidateFacade(FoodVisionService foodVisionService, FoodItemCatalog foodItemCatalog) {
        this.foodVisionService = foodVisionService;
        this.foodItemCatalog = foodItemCatalog;
    }

    public List<FoodCandidate> detect(String photoAssetId) {
        return foodVisionService.detectCandidates(photoAssetId);
    }

    public ConfirmedFood confirm(String photoAssetId, String label) {
        String normalized = label.toLowerCase(Locale.ROOT);
        FoodItem mapped = foodItemCatalog.mapLabel(normalized);

        return new ConfirmedFood(photoAssetId, normalized, mapped, mapped == null ? toTitleCase(normalized) : null);
    }

    private String toTitleCase(String input) {
        String spaced = input.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
