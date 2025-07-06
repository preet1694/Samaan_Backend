package org.samaan.controllers;

import org.samaan.model.User;
import org.samaan.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/name")
    public ResponseEntity<String> getUserName(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(user.getName()))
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        return userService.loginUser(email, password);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body((User) Map.of("error", "User not found")));
    }

    @PostMapping("/getByEmail")
    public ResponseEntity<?> getUserByEmail(@RequestBody Map<String, String> body) {
        return userService.findByEmail(body.get("email"))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body((User) Map.of("error", "User not found")));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
}
