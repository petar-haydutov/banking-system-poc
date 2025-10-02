package com.yotpo.service;

import com.yotpo.account.Account;
import com.yotpo.exception.AccountAlreadyExistsException;
import com.yotpo.exception.AccountNotFoundException;
import com.yotpo.exception.InvalidTransferException;

public interface AccountService {

    /**
     * Clears account cache
     *
     * */
    void clear();

    /**
     * Creates a new account
     *
     * @param account account entity to add or update
     * @throws AccountAlreadyExistsException if account is already present
     * @throws IllegalArgumentException if account data is invalid
     * */
    void createAccount(Account account);

    /**
     * Get account from the cache
     *
     * @param  id identification of an account to search for
     * @return account associated with given id or {@code null} if account is not found in the cache
     * */
    Account getAccount(long id);

    /**
     * Transfers given amount of money from source account to target account
     *
     * @param source account to transfer money from
     * @param target account to transfer money to
     * @param amount dollar amount to transfer
     * @throws InvalidTransferException if transfer request data is invalid
     * @throws AccountNotFoundException if source or target account does not exist
     * */
    void transfer(Account source, Account target, double amount);
}
