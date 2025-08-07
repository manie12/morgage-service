//package io.bank.mortgage.web;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/v1")
//@RequiredArgsConstructor
//public class ApplicationAuthorization {
//    @PostMapping("/auth/refresh")
//    public Mono<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
//        UUID token = UUID.fromString(body.get("refreshToken"));
//        return tokenService.validateRefreshToken(token)
//                .flatMap(user -> Mono.zip(
//                        Mono.just(tokenService.generateAccessToken(user)),
//                        tokenService.generateRefreshToken(user)))
//                .map(t -> Map.of("accessToken", t.getT1(), "refreshToken", t.getT2()))
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
//    }
//}
