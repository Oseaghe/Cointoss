package org.example.cointoss.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cryptocurrencies")
public class Cryptocurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol; // BTC, NGN, USDT

    @Column(name = "name")
    private String name;

    @Column(name = "decimals")
    private int decimals;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "currency")
    private List<Wallet> wallets;
}
