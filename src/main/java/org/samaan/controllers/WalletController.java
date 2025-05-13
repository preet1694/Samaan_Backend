package org.samaan.controllers;

import org.samaan.model.Wallet;
import org.samaan.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<?> createWallet(@RequestParam String userId, @RequestParam String currency) {
        Wallet createdWallet = walletService.createWallet(userId, currency);
        return ResponseEntity.ok(createdWallet);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWalletByUserId(@PathVariable String userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        if (wallet != null) {
            return ResponseEntity.ok(wallet);
        } else {
            return ResponseEntity.badRequest().body("Wallet not found");
        }
    }

    @PutMapping("/{userId}/updatebalance")
    public ResponseEntity<?> updateBalance(@PathVariable String userId, @RequestParam double amount) {
        Wallet updatedWallet = walletService.updateBalance(userId, amount);
        if (updatedWallet != null) {
            return ResponseEntity.ok(updatedWallet);
        } else {
            return ResponseEntity.badRequest().body("Wallet not found");
        }
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<?> getWalletBalance(@PathVariable String userId) {
        Double balance = walletService.getWalletBalance(userId);
        if (balance != null) {
            return ResponseEntity.ok().body(balance);
        } else {
            return ResponseEntity.badRequest().body("Wallet not found");
        }
    }
}
