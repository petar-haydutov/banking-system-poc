package com.yotpo.exception;

import com.yotpo.account.AccountKey;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountKey accountKey) {
        super(String.format("Account with id %s not found", accountKey.getAccountId()));
    }
}
