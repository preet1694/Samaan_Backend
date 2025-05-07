package org.samaan.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.samaan.model.User;
import org.samaan.repositories.UserRepository;
import org.samaan.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private JwtService jwtService;



    @GetMapping("/users")
    public Map<String, Object> user(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) {
            return Map.of("error", "User not authenticated");
        }

        String email = principal.getAttribute("email");
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(email));

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setName(principal.getAttribute("name"));
            newUser.setEmail(email);
            newUser.setRole("sender");
            userRepository.save(newUser);
            existingUser = Optional.of(newUser);
        }

        User user = existingUser.get();
        String jwtToken = jwtService.generateToken(user);

        return Map.of(
                "token", jwtToken,
                "id", user.getId(),
                "name", Objects.requireNonNull(principal.getAttribute("name")),
                "email", email,
                "picture", Objects.requireNonNull(principal.getAttribute("picture")),
                "role", user.getRole()
        );
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@RequestBody Map<String, String> payload) {
        String token = payload.get("credential");

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.toString());

            String email = jsonNode.get("email").asText();
            String name = jsonNode.get("name").asText();
            String picture = jsonNode.get("picture").asText();

            User user = userRepository.findByEmail(email);
            boolean isNew = false;

            if (user == null) {
                user = new User();
                user.setId(email);
                user.setName(name);
                user.setEmail(email);
                user.setProfilePic(picture);
                userRepository.save(user);
                isNew = true;
            }

            String jwtToken = jwtService.generateToken(user);

            Map<String, Object> result = new HashMap<>();
            result.put("token", jwtToken);
            result.put("message", isNew ? "User registered and logged in with Google" : "User successfully logged in with Google");
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("name", user.getName());
            result.put("role", user.getRole());
            result.put("profilePic", user.getProfilePic());
            result.put("isNew", isNew);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Google token"));
        }
    }

    @PostMapping("/google/register")
    public ResponseEntity<?> registerWithGoogle(@RequestBody Map<String, String> payload) {
        String userRole = payload.get("userRole");
        String userEmail = payload.get("userEmail");
        String userName = payload.get("userName");

        // Find user by email
        User user = userRepository.findByEmail(userEmail);
        if (user != null) {
            user.setRole(userRole); // Assign role to the user
            userRepository.save(user);

            String jwtToken = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());
            response.put("profilePic", user.getProfilePic());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found.");
        }
    }
}
