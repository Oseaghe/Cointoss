package org.example.cointoss.dtos;

import jakarta.persistence.*;
import lombok.Data;
import org.example.cointoss.entities.BankAccount;
import org.example.cointoss.entities.Transaction;
import org.example.cointoss.entities.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class WalletDto {
    private Long id;

    private UserDto user;

    private BigDecimal balance;

    private String currency;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private Set<BankAccountDto> bankAccounts;

    private Set<TransactionDto> transactions;
}
