package org.example.cointoss.mappers;

import jakarta.transaction.Transaction;
import org.example.cointoss.dtos.*;
import org.example.cointoss.entities.BankAccount;
import org.example.cointoss.entities.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    @Mapping(source = "customerName", target = "customer.name")
    @Mapping(source = "customerEmail", target = "customer.email")
    CheckoutRequest toCheckoutRequest(FundWalletRequest request);

    TransactionDto toTransactionDto(Transaction transaction);

    @Mapping(source = "wallet.id", target = "walletId")
    @Mapping(source = "id", target = "id")
    BankAccountDto toBankAccountDto(BankAccount bankAccount);

//    @Mapping(source = "transactions.wallet.", target = "transactions.walletId")
    WalletDto toWalletDto(Wallet wallet);

    @Mapping(source = "bankCode", target = "bank")
    @Mapping(source = "accountNumber", target = "account")
    VerifyBankAccountRequest toVerifyBankAccountRequest(CreateBankAccountRequest request);

}
