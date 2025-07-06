package org.samaan.controllers;

import org.samaan.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users")
    public Map<String, Object> user(@AuthenticationPrincipal OidcUser principal) {
        return authService.handleOidcUser(principal);
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@RequestBody Map<String, String> payload) {
        return authService.loginWithGoogle(payload.get("credential"));
    }

    @PostMapping("/google/register")
    public ResponseEntity<?> registerWithGoogle(@RequestBody Map<String, String> payload) {
        String role = payload.get("userRole");
        String email = payload.get("userEmail");
        String name = payload.get("userName");
        return authService.registerWithGoogle(role, email, name);
    }
}
