package com.workflow_builder.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public final class HmacUtils {

    private static final String HMAC_ALGO = "HmacSHA256";

    public static String computeHmacSha256Base64(String secret, byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), HMAC_ALGO);
            mac.init(keySpec);
            byte[] h = mac.doFinal(payload);
            return Base64.getEncoder().encodeToString(h);
        } catch (Exception e) {
            throw new RuntimeException("HMAC failure", e);
        }
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}
