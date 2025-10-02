package com.yotpo.exception;

import com.yotpo.account.AccountKey;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String msg) {
        super(String.format("Account with id %s has insufficient balance for this transaction to occur", msg));
    }
}
