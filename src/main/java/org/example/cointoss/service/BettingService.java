// src/main/java/org/example/cointoss/service/BettingService.java
package org.example.cointoss.service;

import lombok.RequiredArgsConstructor;
import org.example.cointoss.entities.Bets;
import org.example.cointoss.entities.BettingPools;
import org.example.cointoss.entities.User;
import org.example.cointoss.entities.Wallet;
import org.example.cointoss.repositories.BetsRepository;
import org.example.cointoss.repositories.BettingPoolsRepository;
import org.example.cointoss.repositories.UserRepository;
import org.example.cointoss.repositories.WalletRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // Lombok creates a constructor with all final fields
public class BettingService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BettingPoolsRepository bettingPoolsRepository;
    private final BetsRepository betsRepository;
    private final PriceService priceService;

    // This annotation is CRITICAL. It ensures that all database operations within this method
    // either all succeed, or all fail together. This prevents data corruption, like a user's
    // balance being debited without their bet being recorded.
    @Transactional
    public void placeBet(Long poolId, BigDecimal amount, String direction) {
        // 1. Get the currently authenticated user's ID from the security context.
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Fetch the necessary entities from the database.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        BettingPools pool = bettingPoolsRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Betting pool not found"));

        // 3. Perform business logic validations.
        if (!"OPEN".equals(pool.getStatus())) {
            throw new IllegalStateException("Betting pool is not open for bets.");
        }
        if (!"UP".equalsIgnoreCase(direction) && !"DOWN".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException("Invalid direction. Must be 'UP' or 'DOWN'.");
        }

        // 4. Check and update the user's wallet balance.
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 5. Create the new bet record.
        Bets newBet = new Bets();
        newBet.setUser(user);
        newBet.setPool(pool);
        newBet.setAmount(amount);
        newBet.setDirection(direction.toUpperCase());
        newBet.setStatus("PENDING");
        betsRepository.save(newBet);

        // 6. Update the pool totals.
        if ("UP".equalsIgnoreCase(direction)) {
            pool.setTotalUpPool(pool.getTotalUpPool().add(amount));
        } else {
            pool.setTotalDownPool(pool.getTotalDownPool().add(amount));
        }
        bettingPoolsRepository.save(pool);
    }

    /**
     * Creates a new betting pool for the next 10-minute cycle.
     */
    @Transactional
    public void createNextPool() {
        // In the future, you could check if another pool is already open.
        // For now, we'll just create a new one.
        String assetPair = "BTC/USDT";

        BigDecimal startPrice = priceService.getCurrentPrice(assetPair);

        BettingPools newPool = new BettingPools();
        newPool.setAssetPair(assetPair);
        newPool.setStatus("OPEN");
        newPool.setStartPrice(startPrice);

        // Set the times for our 10-minute cycle
        OffsetDateTime now = OffsetDateTime.now();
        newPool.setOpenTime(now);
        newPool.setLockTime(now.plusMinutes(5));
        newPool.setSettlementTime(now.plusMinutes(10));

        bettingPoolsRepository.save(newPool);
        System.out.println("Created new betting pool with start price: " + startPrice);
    }

    /**
     * Finds any open pools whose lock time has passed and changes their status to "LOCKED".
     */
    @Transactional
    public void lockDuePools() {
        List<BettingPools> poolsToLock = bettingPoolsRepository.findAllByStatusAndLockTimeBefore("OPEN", OffsetDateTime.now());
        for (BettingPools pool : poolsToLock) {
            pool.setStatus("LOCKED");
            bettingPoolsRepository.save(pool);
            System.out.println("Locked pool with ID: " + pool.getId());
        }
    }

    /**
     * Finds any locked pools whose settlement time has passed and settles them.
     */
    @Transactional
    public void settleDuePools() {
        List<BettingPools> poolsToSettle = bettingPoolsRepository.findAllByStatusAndSettlementTimeBefore("LOCKED", OffsetDateTime.now());
        for (BettingPools pool : poolsToSettle) {
            settlePool(pool);
        }
    }

    /**
     * The core settlement logic for a single pool.
     */
    private void settlePool(BettingPools pool) {
        BigDecimal endPrice = priceService.getCurrentPrice(pool.getAssetPair());
        pool.setEndPrice(endPrice);

        String winningDirection;
        // The price went up if endPrice is greater than startPrice
        if (endPrice.compareTo(pool.getStartPrice()) > 0) {
            winningDirection = "UP";
        } else {
            winningDirection = "DOWN";
        }

        BigDecimal totalPool = pool.getTotalUpPool().add(pool.getTotalDownPool());
        BigDecimal winningsPool = totalPool.multiply(new BigDecimal("0.95")); // 5% house rake

        BigDecimal winningSideTotal = "UP".equals(winningDirection) ? pool.getTotalUpPool() : pool.getTotalDownPool();

        // Find all bets for this pool
        List<Bets> betsInPool = betsRepository.findAllByPoolId(pool.getId());

        for (Bets bet : betsInPool) {
            if (bet.getDirection().equals(winningDirection)) {
                // This is a winning bet
                if (winningSideTotal.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal proportion = bet.getAmount().divide(winningSideTotal, 10, RoundingMode.HALF_UP);
                    BigDecimal payout = winningsPool.multiply(proportion);
                    
                    bet.setPayout(payout);
                    bet.setStatus("WON");

                    // Credit the user's wallet
                    Wallet wallet = walletRepository.findByUserId(bet.getUser().getId()).orElseThrow();
                    wallet.setBalance(wallet.getBalance().add(payout));
                    walletRepository.save(wallet);
                } else {
                    // This case handles if there are winners but the winning pool was 0, they get their money back.
                    bet.setPayout(bet.getAmount());
                    bet.setStatus("WON"); // Technically a push/refund
                    Wallet wallet = walletRepository.findByUserId(bet.getUser().getId()).orElseThrow();
                    wallet.setBalance(wallet.getBalance().add(bet.getAmount()));
                    walletRepository.save(wallet);
                }
            } else {
                // This is a losing bet
                bet.setStatus("LOST");
                bet.setPayout(BigDecimal.ZERO);
            }
            betsRepository.save(bet);
        }

        pool.setStatus("SETTLED");
        bettingPoolsRepository.save(pool);
        System.out.println("Settled pool ID " + pool.getId() + " with end price " + endPrice + ". Winning direction: " + winningDirection);
    }
}