package com.example.zorvyn.auth.service;

import com.example.zorvyn.auth.dto.AuthToken;
import com.example.zorvyn.auth.dto.TokenPayload;
import com.example.zorvyn.common.model.RoleType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

    @Value("${security.token.secret}")
    private String secret;

    @Value("${security.token.ttl-minutes:90}")
    private long ttlMinutes;

    public AuthToken generateToken(Long userId, RoleType role) {
        Instant expiresAt = Instant.now().plusSeconds(ttlMinutes * 60L);
        String sessionId = randomHex(16);
        String salt = randomBase64(12);
        String payloadText = sessionId + ":" + userId + ":" + role.name() + ":" + expiresAt.getEpochSecond() + ":" + salt;
        String encodedPayload = base64UrlEncoder.encodeToString(payloadText.getBytes(StandardCharsets.UTF_8));
        String encodedSignature = base64UrlEncoder.encodeToString(hmac(payloadText));
        String token = encodedPayload + "." + encodedSignature;

        return new AuthToken(token, hashToken(token), sessionId, salt, expiresAt);
    }

    public TokenPayload parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Token format is invalid.");
        }

        String payloadText;
        try {
            payloadText = new String(base64UrlDecoder.decode(parts[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token payload encoding is invalid.", ex);
        }

        String expectedSignature = base64UrlEncoder.encodeToString(hmac(payloadText));
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Token signature is invalid.");
        }

        String[] chunks = payloadText.split(":");
        if (chunks.length != 5) {
            throw new IllegalArgumentException("Token payload structure is invalid.");
        }

        long exp = Long.parseLong(chunks[3]);
        if (Instant.now().isAfter(Instant.ofEpochSecond(exp))) {
            throw new IllegalArgumentException("Token has expired.");
        }

        return new TokenPayload(
                chunks[0],
                Long.parseLong(chunks[1]),
                RoleType.valueOf(chunks[2]),
                exp,
                chunks[4]
        );
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncoder.encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String randomBase64(int bytes) {
        byte[] data = new byte[bytes];
        secureRandom.nextBytes(data);
        return base64UrlEncoder.encodeToString(data);
    }

    private String randomHex(int bytes) {
        byte[] data = new byte[bytes];
        secureRandom.nextBytes(data);
        StringBuilder builder = new StringBuilder(data.length * 2);
        for (byte b : data) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private byte[] hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }
}



