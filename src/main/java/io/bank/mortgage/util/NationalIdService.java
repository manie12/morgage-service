package io.bank.mortgage.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Utility for hashing (deterministic) and encrypting (non-deterministic) national IDs.
 */
@Component
@RequiredArgsConstructor
public class NationalIdService {

    private final byte[] hmacKey;
    private final byte[] aesKey;   // 16/24/32-byte key for AES-GCM

    public NationalIdService(@Value("${security.nationalId.hmacKey}") String hmac,
                             @Value("${security.nationalId.encKey}") String aes) {
        this.hmacKey = hmac.getBytes(StandardCharsets.UTF_8);
        this.aesKey = aes.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Hex-encoded HMAC-SHA256 for equality searches
     */
    public String hash(String id) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            byte[] raw = mac.doFinal(id.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }

    /**
     * AES-GCM encryption (IV || cipherText || tag) for secure storage
     */
    public byte[] encrypt(String id) {
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(128, iv));
            byte[] cipherText = cipher.doFinal(id.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return combined;
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failure", e);
        }
    }
}