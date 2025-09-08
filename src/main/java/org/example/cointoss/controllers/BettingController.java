// src/main/java/org/example/cointoss/controllers/BettingController.java
package org.example.cointoss.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cointoss.dtos.PlaceBetRequest;
import org.example.cointoss.service.BettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BettingController {

    private final BettingService bettingService;

    @PostMapping
    public ResponseEntity<Void> placeBet(@Valid @RequestBody PlaceBetRequest request) {
        try {
            bettingService.placeBet(request.getPoolId(), request.getAmount(), request.getDirection());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Catches business logic errors (e.g., "insufficient funds") and returns a bad request status.
            return ResponseEntity.badRequest().header("X-Error-Message", e.getMessage()).build();
        }
    }
}