package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.ResourceDtos.SocialProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/social")
public class SocialController {
    @GetMapping("/profile")
    public SocialProfileResponse profile() {
        return new SocialProfileResponse("demo-user", "foodie", "hello foodpic");
    }
}
