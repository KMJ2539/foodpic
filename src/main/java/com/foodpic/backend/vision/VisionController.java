package com.foodpic.backend.vision;

import java.util.List;

/**
 * HTTP framework adapter placeholder.
 * POST /v1/vision/food-candidates -> detectCandidates
 * POST /v1/vision/food-confirm -> confirmCandidate
 */
public class VisionController {
    public static final String FOOD_CANDIDATES_PATH = "/v1/vision/food-candidates";
    public static final String FOOD_CONFIRM_PATH = "/v1/vision/food-confirm";

    private final VisionFoodCandidateFacade visionFoodCandidateFacade;

    public VisionController(VisionFoodCandidateFacade visionFoodCandidateFacade) {
        this.visionFoodCandidateFacade = visionFoodCandidateFacade;
    }

    public DetectCandidatesResponse detectCandidates(DetectCandidatesRequest request) {
        List<FoodCandidate> candidates = visionFoodCandidateFacade.detect(request.photoAssetId());
        return new DetectCandidatesResponse(candidates);
    }

    public ConfirmFoodResponse confirmCandidate(ConfirmFoodRequest request) {
        ConfirmedFood confirmed = visionFoodCandidateFacade.confirm(request.photoAssetId(), request.label());
        return new ConfirmFoodResponse(confirmed);
    }

    public record DetectCandidatesRequest(String photoAssetId) {}
    public record DetectCandidatesResponse(List<FoodCandidate> candidates) {}
    public record ConfirmFoodRequest(String photoAssetId, String label) {}
    public record ConfirmFoodResponse(ConfirmedFood confirmed) {}
}
