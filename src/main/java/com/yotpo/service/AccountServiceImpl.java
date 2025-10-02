package com.yotpo.service;

import com.yotpo.account.Account;
import com.yotpo.account.AccountKey;
import com.yotpo.exception.AccountAlreadyExistsException;
import com.yotpo.exception.AccountNotFoundException;
import com.yotpo.exception.InsufficientBalanceException;
import com.yotpo.exception.InvalidTransferException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AccountServiceImpl implements AccountService {

    private final ConcurrentMap<AccountKey, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        accounts.clear();
    }

    @Override
    public void createAccount(Account account) {
        if (account == null || account.getAccountKey() == null) {
            throw new IllegalArgumentException("Account and AccountKey cannot be null");
        }

        if (account.getFirstName().isEmpty() || account.getLastName().isEmpty()) {
            throw new IllegalArgumentException("First and last names must not be empty");
        }

        if (account.getBalance() == null || account.getBalance() < 0) {
            throw new IllegalArgumentException("Balance must not be negative");
        }

        if (accounts.containsKey(account.getAccountKey())) {
            throw new AccountAlreadyExistsException(account.getAccountKey());
        }

        accounts.put(account.getAccountKey(), account);
    }

    @Override
    public Account getAccount(long id) {
        return accounts.get(AccountKey.valueOf(id));
    }

    @Override
    public void transfer(Account source, Account target, double amount) {
        validateTransferRequest(source, target, amount);

        Account sourceAccount = getAccount(source.getAccountKey().getAccountId());
        if (sourceAccount == null) {
            throw new AccountNotFoundException(source.getAccountKey());
        }

        if (sourceAccount.getBalance() < amount) {
            throw new InsufficientBalanceException(source.getAccountKey());
        }

        Account targetAccount = getAccount(target.getAccountKey().getAccountId());
        if (targetAccount == null) {
            throw new AccountNotFoundException(target.getAccountKey());
        }

        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        targetAccount.setBalance(targetAccount.getBalance() + amount);
    }

    private void validateTransferRequest(Account source, Account target, Double amount) {
        if (source == null || target == null) {
            throw new InvalidTransferException("Source and target accounts must not be null");
        }

        if (source.equals(target)) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }

        if (amount == null || amount <= 0) {
            throw new InvalidTransferException("Transfer amount must be positive");
        }
    }
}
