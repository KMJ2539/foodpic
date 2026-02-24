package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.VisionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/vision")
public class VisionController {
    @GetMapping("/{photoId}")
    public VisionResponse analyze(@PathVariable String photoId) {
        return new VisionResponse(photoId, List.of("salad", "avocado"), 0.98);
    }
}
