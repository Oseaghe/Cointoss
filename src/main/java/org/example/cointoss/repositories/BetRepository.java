// src/main/java/org/example/cointoss/repositories/BetsRepository.java
package org.example.cointoss.repositories;

import org.example.cointoss.entities.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {
    // We can add methods to find bets by user, etc., later.
    List<Bet> findAllByPoolId(Long poolId);
}