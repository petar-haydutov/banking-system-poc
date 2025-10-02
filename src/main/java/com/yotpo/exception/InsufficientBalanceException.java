package com.yotpo.exception;

import com.yotpo.account.AccountKey;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(AccountKey accountKey) {
        super(String.format("Account with id %s has insufficient balance for this transaction to occur", accountKey.getAccountId()));
    }
}
