package io.bank.mortgage.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class
NationalIdServiceImpl {
  @Value("${security.nationalId.hmacKey}") private String hmacKey;
  @Value("${security.nationalId.encKey}") private String encKey;

  public String hash(String raw) { /* HMAC-SHA256 to hex */ }
  public byte[] encrypt(String raw) { /* AES-GCM with stable IV derivation is unsafe; use random IV + store tag. For deterministic lookup, rely on hash; use encrypt only for internal display or retrieval. */ }
  public String mask(String rawOrDecrypted) { return rawOrDecrypted.replaceAll(".(?=.{3}$)", "*"); }
}