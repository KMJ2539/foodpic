package com.foodpic.backend.vision;

import java.util.List;

public interface FoodVisionService {
    List<FoodCandidate> detectCandidates(String photoAssetId);
}
