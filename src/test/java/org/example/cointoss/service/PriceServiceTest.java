package org.example.cointoss.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceServiceTest {
    private final PriceService priceService = new PriceService();

    @Test
    void getCurrentPrice_returnsPriceInExpectedRange() {
        String assetPair = "BTC/USDT";
        for (int i = 0; i < 20; i++) {
            BigDecimal price = priceService.getCurrentPrice(assetPair);
            assertTrue(price.compareTo(new BigDecimal("64350.00")) >= 0,
                    "Price should not be less than 64350.00, got: " + price);
            assertTrue(price.compareTo(new BigDecimal("65650.00")) <= 0,
                    "Price should not be more than 65650.00, got: " + price);
        }
    }

    @Test
    void getCurrentPrice_handlesDifferentAssetPairs() {
        String[] pairs = {"BTC/USDT", "ETH/USDT", "DOGE/USDT", "BNB/USDT"};
        for (String pair : pairs) {
            BigDecimal price = priceService.getCurrentPrice(pair);
            assertNotNull(price);
        }
    }

    @Test
    void getCurrentPrice_handlesNullOrEmptyAssetPair() {
        assertNotNull(priceService.getCurrentPrice(null));
        assertNotNull(priceService.getCurrentPrice(""));
    }
}
