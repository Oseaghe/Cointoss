package org.example.cointoss.entities;

public enum CycleStatus {
    OPEN,       // 0–5 min, users can place bets
    LOCKED,     // 5–10 min, no more bets, waiting for outcome
    SETTLED,    // cycle finished, payouts/commissions calculated
    CANCELED,   // cycle canceled (e.g., oracle failure, one-sided pool)
    PUSH
}
