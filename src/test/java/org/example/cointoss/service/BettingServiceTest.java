package org.example.cointoss.service;

import org.example.cointoss.entities.*;
import org.example.cointoss.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BettingServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private BettingPoolRepository bettingPoolRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private PriceService priceService;

    @InjectMocks
    private BettingService bettingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock security context and authentication
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(1L); // principal = 1L
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void placeBet_successful() {
        // Arrange
        User user = new User(); user.setId(1L); user.setEmail("test@example.com");
        Wallet wallet = new Wallet(); wallet.setUser(user); wallet.setBalance(new BigDecimal("100.00"));
        BettingPool pool = new BettingPool();
        pool.setId(10L); pool.setStatus("OPEN");
        pool.setOpenTime(OffsetDateTime.now().minusMinutes(1));
        pool.setLockTime(OffsetDateTime.now().plusMinutes(4));
        pool.setTotalUpPool(BigDecimal.ZERO); pool.setTotalDownPool(BigDecimal.ZERO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(bettingPoolRepository.findById(10L)).thenReturn(Optional.of(pool));

        // Act
        bettingService.placeBet(10L, new BigDecimal("10.00"), "UP");

        // Assert
        assertEquals(new BigDecimal("90.00"), wallet.getBalance());
        verify(betRepository).save(any(Bet.class));
        verify(walletRepository).save(wallet);
        verify(bettingPoolRepository).save(pool);
    }

    @Test
    void placeBet_fails_whenInsufficientFunds() {
        User user = new User(); user.setId(1L);
        Wallet wallet = new Wallet(); wallet.setUser(user); wallet.setBalance(new BigDecimal("5.00"));
        BettingPool pool = new BettingPool();
        pool.setId(10L); pool.setStatus("OPEN");
        pool.setOpenTime(OffsetDateTime.now().minusMinutes(1));
        pool.setLockTime(OffsetDateTime.now().plusMinutes(4));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(bettingPoolRepository.findById(10L)).thenReturn(Optional.of(pool));

        Exception ex = assertThrows(IllegalStateException.class, () ->
                bettingService.placeBet(10L, new BigDecimal("10.00"), "UP"));
        assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    // Add more tests for closed period, invalid direction, etc.
}
