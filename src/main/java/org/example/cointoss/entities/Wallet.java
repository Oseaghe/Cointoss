package org.example.cointoss.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Cryptocurrency currency;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal balance = BigDecimal.valueOf(100.00);

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "wallet")
    private List<Transaction> transactions = new LinkedList<>();
}
