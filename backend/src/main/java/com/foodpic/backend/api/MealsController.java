package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.MealResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/meals")
public class MealsController {
    @GetMapping("/{mealId}")
    public MealResponse get(@PathVariable String mealId) {
        return new MealResponse(mealId, "post-1", "lunch", 650);
    }
}
