package org.example.cointoss.entities;

public enum BetStatus {
    PENDING,   // bet placed, cycle not yet finished
    WON,       // bet was correct, payout credited
    LOST,      // bet was incorrect, funds lost
    REFUNDED
}
