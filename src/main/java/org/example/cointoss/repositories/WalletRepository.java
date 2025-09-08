// src/main/java/org/example/cointoss/repositories/WalletRepository.java
package org.example.cointoss.repositories;

import org.example.cointoss.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    // A crucial method to find a wallet by its user's ID.
    Optional<Wallet> findByUserId(Long userId);
}