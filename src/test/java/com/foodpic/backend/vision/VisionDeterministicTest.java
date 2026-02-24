package com.foodpic.backend.vision;

import java.util.List;

public class VisionDeterministicTest {
    public static void main(String[] args) {
        FoodItemCatalog catalog = new FoodItemCatalog();
        StubFoodVisionService service = new StubFoodVisionService(catalog);
        VisionFoodCandidateFacade facade = new VisionFoodCandidateFacade(service, catalog);
        VisionController controller = new VisionController(facade);

        String input = "bucket/images/lunch_kimchi_001.jpg";
        List<FoodCandidate> first = service.detectCandidates(input);
        List<FoodCandidate> second = service.detectCandidates(input);
        assert first.equals(second) : "detectCandidates should be deterministic";

        VisionController.DetectCandidatesRequest request = new VisionController.DetectCandidatesRequest("hash://assets/banana.png");
        VisionController.DetectCandidatesResponse firstResponse = controller.detectCandidates(request);
        VisionController.DetectCandidatesResponse secondResponse = controller.detectCandidates(request);
        assert firstResponse.equals(secondResponse) : "endpoint adapter should be deterministic";

        ConfirmedFood unknown = facade.confirm("photo-1", "mystery_noodle");
        assert unknown.foodItem() == null : "fallback should not map unknown labels";
        assert "Mystery noodle".equals(unknown.freeTextName()) : "fallback free text should be title-cased";

        System.out.println("VisionDeterministicTest passed");
    }
}
