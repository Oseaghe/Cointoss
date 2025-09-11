package org.example.cointoss.service;

import org.example.cointoss.dtos.TickerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class QuidaxPaymentGatewayTest {
    @Spy
    private QuidaxPaymentGateway quidaxPaymentGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set baseUrl to a dummy value to avoid accidental real calls
        quidaxPaymentGateway.baseUrl = "https://dummy-quidax.com";
        quidaxPaymentGateway.apiKey = "dummy-key";
    }

    @Test
    void getBuyPrice_returnsNullOnError() {
        // This will fail to connect, so should return null
        TickerResponse response = quidaxPaymentGateway.getBuyPrice("BTCUSDT");
        assertNull(response);
    }

    @Test
    void getBuyPrice_handlesInvalidMarketPair() {
        TickerResponse response = quidaxPaymentGateway.getBuyPrice("");
        assertNull(response);
    }
}
