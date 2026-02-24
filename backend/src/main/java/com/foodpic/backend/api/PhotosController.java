package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.PhotoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/photos")
public class PhotosController {
    @GetMapping("/{photoId}")
    public PhotoResponse get(@PathVariable String photoId) {
        return new PhotoResponse(photoId, "post-1", "https://cdn.foodpic.local/photos/1.jpg");
    }
}
