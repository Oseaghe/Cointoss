// src/main/java/org/example/cointoss/repositories/BettingPoolsRepository.java
package org.example.cointoss.repositories;

import org.example.cointoss.entities.BettingPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BettingPoolRepository extends JpaRepository<BettingPool, Long> {
    // This custom query will help us find the latest pool that is currently open for betting.
    Optional<BettingPool> findFirstByStatusOrderByOpenTimeDesc(String status);

    List<BettingPool> findAllByStatusAndLockTimeBefore(String status, OffsetDateTime time);
    List<BettingPool> findAllByStatusAndSettlementTimeBefore(String status, OffsetDateTime time);

}