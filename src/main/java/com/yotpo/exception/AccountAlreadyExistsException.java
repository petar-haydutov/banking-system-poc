package com.yotpo.exception;

import com.yotpo.account.AccountKey;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(AccountKey accountKey) {
        super(String.format("Account with id %s already exists", accountKey.getAccountId()));
    }
}
