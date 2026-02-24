package com.foodpic.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtTokenService {
    private final byte[] secret;

    public JwtTokenService(@Value("${security.jwt.secret:test-fixed-secret-key-1234567890}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(String subject, long expiresSeconds) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        long exp = Instant.now().getEpochSecond() + expiresSeconds;
        String payload = base64Url("{\"sub\":\"" + subject + "\",\"exp\":" + exp + "}");
        String signingInput = header + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    public String validateAndGetSubject(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!sign(signingInput).equals(parts[2])) {
            return null;
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        if (!payload.contains("\"sub\":\"")) {
            return null;
        }
        long exp = extractLong(payload, "exp");
        if (exp < Instant.now().getEpochSecond()) {
            return null;
        }
        return extractString(payload, "sub");
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign token", e);
        }
    }

    private String base64Url(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String extractString(String json, String key) {
        String needle = "\"" + key + "\":\"";
        int start = json.indexOf(needle);
        if (start < 0) return null;
        start += needle.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private long extractLong(String json, String key) {
        String needle = "\"" + key + "\":";
        int start = json.indexOf(needle);
        if (start < 0) return 0;
        start += needle.length();
        int end = json.indexOf('}', start);
        return Long.parseLong(json.substring(start, end).replaceAll("[^0-9]", ""));
    }
}
