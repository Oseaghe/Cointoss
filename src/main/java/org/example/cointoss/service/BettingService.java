// src/main/java/org/example/cointoss/service/BettingService.java
package org.example.cointoss.service;

import lombok.RequiredArgsConstructor;
import org.example.cointoss.entities.Bet;
import org.example.cointoss.entities.BettingPool;
import org.example.cointoss.entities.User;
import org.example.cointoss.entities.Wallet;
import org.example.cointoss.repositories.BetRepository;
import org.example.cointoss.repositories.BettingPoolRepository;
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
    private final BettingPoolRepository bettingPoolRepository;
    private final BetRepository betRepository;
    private final PriceService priceService;

    // This annotation is CRITICAL. It ensures that all database operations within this method
    // either all succeed, or all fail together. This prevents data corruption, like a user's
    // balance being debited without their bet being recorded.
    @Transactional
    public void placeBet(Long poolId, BigDecimal amount, String direction) {
        // 1. Get the currently authenticated user's ID from the security context.
        Long userId = getAuthenticatedUserId();

        // 2. Fetch the necessary entities from the database.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        BettingPool pool = bettingPoolRepository.findById(poolId)
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
        Bet newBet = new Bet();
        newBet.setUser(user);
        newBet.setPool(pool);
        newBet.setAmount(amount);
        newBet.setDirection(direction.toUpperCase());
        newBet.setStatus("PENDING");
        betRepository.save(newBet);

        // 6. Update the pool totals.
        if ("UP".equalsIgnoreCase(direction)) {
            pool.setTotalUpPool(pool.getTotalUpPool().add(amount));
        } else {
            pool.setTotalDownPool(pool.getTotalDownPool().add(amount));
        }
        bettingPoolRepository.save(pool);
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

        BettingPool newPool = new BettingPool();
        newPool.setAssetPair(assetPair);
        newPool.setStatus("OPEN");
        newPool.setStartPrice(startPrice);

        // Set the times for our 10-minute cycle
        OffsetDateTime now = OffsetDateTime.now();
        newPool.setOpenTime(now);
        newPool.setLockTime(now.plusMinutes(5));
        newPool.setSettlementTime(now.plusMinutes(10));

        bettingPoolRepository.save(newPool);
        System.out.println("Created new betting pool with start price: " + startPrice);
    }

    /**
     * Finds any open pools whose lock time has passed and changes their status to "LOCKED".
     */
    @Transactional
    public void lockDuePools() {
        List<BettingPool> poolsToLock = bettingPoolRepository.findAllByStatusAndLockTimeBefore("OPEN", OffsetDateTime.now());
        for (BettingPool pool : poolsToLock) {
            pool.setStatus("LOCKED");
            bettingPoolRepository.save(pool);
            System.out.println("Locked pool with ID: " + pool.getId());
        }
    }

    /**
     * Finds any locked pools whose settlement time has passed and settles them.
     */
    @Transactional
    public void settleDuePools() {
        List<BettingPool> poolsToSettle = bettingPoolRepository.findAllByStatusAndSettlementTimeBefore("LOCKED", OffsetDateTime.now());
        for (BettingPool pool : poolsToSettle) {
            settlePool(pool);
        }
    }

    /**
     * The core settlement logic for a single pool.
     */
    private void settlePool(BettingPool pool) {
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
        List<Bet> betsInPool = betRepository.findAllByPoolId(pool.getId());

        for (Bet bet : betsInPool) {
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
            betRepository.save(bet);
        }

        pool.setStatus("SETTLED");
        bettingPoolRepository.save(pool);
        System.out.println("Settled pool ID " + pool.getId() + " with end price " + endPrice + ". Winning direction: " + winningDirection);
    }

    /**
     * Settle a betting pool: determine outcome, handle edge cases, calculate payouts, and update wallets.
     */
    @Transactional
    public void settlePool(Long poolId) {
        BettingPool pool = bettingPoolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Betting pool not found"));

        // Only settle if not already settled
        if ("SETTLED".equals(pool.getStatus())) {
            throw new IllegalStateException("Pool already settled");
        }

        // Fetch all bets for this pool
        List<Bet> bets = betRepository.findAllByPoolId(poolId);
        if (bets.isEmpty()) {
            pool.setStatus("CANCELED");
            bettingPoolRepository.save(pool);
            return;
        }

        // Fetch start and end prices
        BigDecimal startPrice = pool.getStartPrice();
        BigDecimal endPrice = pool.getEndPrice();
        if (startPrice == null || endPrice == null) {
            // Price fetch failed, refund all bets
            for (Bet bet : bets) {
                refundBet(bet);
            }
            pool.setStatus("CANCELED");
            bettingPoolRepository.save(pool);
            return;
        }

        // Edge case: PUSH (tie)
        if (startPrice.compareTo(endPrice) == 0) {
            for (Bet bet : bets) {
                refundBet(bet);
            }
            pool.setStatus("SETTLED");
            bettingPoolRepository.save(pool);
            return;
        }

        // Edge case: all bets on one side
        boolean allUp = bets.stream().allMatch(b -> "UP".equalsIgnoreCase(b.getDirection()));
        boolean allDown = bets.stream().allMatch(b -> "DOWN".equalsIgnoreCase(b.getDirection()));
        if (allUp || allDown) {
            for (Bet bet : bets) {
                refundBet(bet);
            }
            pool.setStatus("CANCELED");
            bettingPoolRepository.save(pool);
            return;
        }

        // Determine outcome
        String outcome = endPrice.compareTo(startPrice) > 0 ? "UP" : "DOWN";
        pool.setStatus("SETTLED");
        bettingPoolRepository.save(pool);

        // Calculate pools and commission
        BigDecimal totalUp = pool.getTotalUpPool();
        BigDecimal totalDown = pool.getTotalDownPool();
        BigDecimal totalPool = totalUp.add(totalDown);
        BigDecimal commission = totalPool.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payoutPool = totalPool.subtract(commission);

        // Calculate dynamic payout multiplier
        BigDecimal winnerPool = "UP".equals(outcome) ? totalUp : totalDown;
        BigDecimal loserPool = "UP".equals(outcome) ? totalDown : totalUp;
        BigDecimal multiplier = winnerPool.compareTo(BigDecimal.ZERO) > 0
                ? payoutPool.divide(winnerPool, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Settle each bet
        for (Bet bet : bets) {
            if (outcome.equalsIgnoreCase(bet.getDirection())) {
                // Winner
                BigDecimal payout = bet.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                creditWinnings(bet, payout);
                bet.setStatus("WON");
                bet.setPayout(payout);
            } else {
                // Loser
                bet.setStatus("LOST");
                bet.setPayout(BigDecimal.ZERO);
            }
            betRepository.save(bet);
        }
    }

    private void refundBet(Bet bet) {
        Wallet wallet = walletRepository.findByUserId(bet.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));
        wallet.setBalance(wallet.getBalance().add(bet.getAmount()));
        walletRepository.save(wallet);
        bet.setStatus("REFUNDED");
        bet.setPayout(bet.getAmount());
        betRepository.save(bet);
    }

    private void creditWinnings(Bet bet, BigDecimal payout) {
        Wallet wallet = walletRepository.findByUserId(bet.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));
        wallet.setBalance(wallet.getBalance().add(payout));
        walletRepository.save(wallet);
    }

    /**
     * Safely extract the authenticated user's ID from the security context.
     * Throws IllegalStateException if not found or invalid.
     */
    private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            // If using UserDetails, fetch user by email/username
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));
        } else {
            throw new IllegalStateException("Invalid authentication principal type: " + principal.getClass().getName());
        }
    }
}