package com.yotpo.service;

import com.yotpo.account.Account;
import com.yotpo.account.AccountKey;
import com.yotpo.exception.AccountAlreadyExistsException;
import com.yotpo.exception.AccountNotFoundException;
import com.yotpo.exception.InsufficientBalanceException;
import com.yotpo.exception.InvalidTransferException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountServiceImpl();
    }

    @AfterEach
    public void cleanUp() {
        accountService.clear();
    }

    @Test
    public void testClear() {
        // Given: accounts exist in the service
        Account account1 = new Account(AccountKey.valueOf(1L), "Pesho", "P", 100.0);
        Account account2 = new Account(AccountKey.valueOf(2L), "Gosho", "G", 200.0);

        accountService.createAccount(account1);
        accountService.createAccount(account2);

        // Verify accounts exist
        assertNotNull(accountService.getAccount(1L));
        assertNotNull(accountService.getAccount(2L));

        // When: clear is called
        accountService.clear();

        // Then: all accounts should be removed
        assertNull(accountService.getAccount(1L));
        assertNull(accountService.getAccount(2L));
    }

    @Test
    public void testCreateAccount() {
        // Given: a new account
        Account account = new Account(AccountKey.valueOf(1L), "Pesho", "G", 100.0);

        // When: creating the account
        accountService.createAccount(account);

        // Then: the account should be retrievable
        Account retrievedAccount = accountService.getAccount(1L);
        assertNotNull(retrievedAccount);
        assertEquals(account.getAccountKey(), retrievedAccount.getAccountKey());
        assertEquals(account.getFirstName(), retrievedAccount.getFirstName());
        assertEquals(account.getLastName(), retrievedAccount.getLastName());
        assertEquals(account.getBalance(), retrievedAccount.getBalance());
    }

    @Test
    public void testCreateAccountThrowsExceptionForDuplicate() {
        // Given: an existing account
        Account account1 = new Account(AccountKey.valueOf(1L), "Joro", "J", 300.0);
        accountService.createAccount(account1);

        // When: attempting to create another account with the same key
        Account account2 = new Account(AccountKey.valueOf(1L), "Misho", "M", 200.0);

        // Then: should throw AccountAlreadyExistsException
        assertThrows(AccountAlreadyExistsException.class, () -> accountService.createAccount(account2));
    }

    @Test
    public void testCreateAccountThrowsExceptionForEmptyFirstOrLastName() {
        // Given: accounts with empty First or Last names
        Account account1 = new Account(AccountKey.valueOf(1L), "", "J", 300.0);
        Account account2 = new Account(AccountKey.valueOf(1L), "Misho", "", 200.0);

        // When: attempting to create them
        // Then: should throw AccountAlreadyExistsException
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account1));
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account2));
    }

    @Test
    public void testCreateAccountThrowsExceptionForNegativeBalance() {
        // Given: accounts with negative balance
        Account account1 = new Account(AccountKey.valueOf(1L), "Misho", "M", (double) -15);

        // When: attempting to create it
        // Then: should throw AccountAlreadyExistsException
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account1));
    }

    @Test
    public void testGetAccountExists() {
        // Given: an account exists
        Account account = new Account(AccountKey.valueOf(1L), "Misho", "M", 400.0);
        accountService.createAccount(account);

        // When: retrieving the account
        Account retrievedAccount = accountService.getAccount(1L);

        // Then: should return the correct account
        assertNotNull(retrievedAccount);
        assertEquals(account.getAccountKey(), retrievedAccount.getAccountKey());
        assertEquals(account.getFirstName(), retrievedAccount.getFirstName());
        assertEquals(account.getLastName(), retrievedAccount.getLastName());
        assertEquals(account.getBalance(), retrievedAccount.getBalance());
    }

    @Test
    public void testGetAccountNotExists() {
        // Given: no accounts exist

        // When: retrieving a non-existent account
        Account retrievedAccount = accountService.getAccount(999L);

        // Then: should return null
        assertNull(retrievedAccount);
    }

    @Test
    public void testMultipleAccountsOperations() {
        // Given: multiple accounts
        Account account1 = new Account(AccountKey.valueOf(1L), "Pesho", "P", 100.0);
        Account account2 = new Account(AccountKey.valueOf(2L), "Gosho", "G", 200.0);
        Account account3 = new Account(AccountKey.valueOf(3L), "Misho", "M", 300.0);

        // When: creating multiple accounts
        accountService.createAccount(account1);
        accountService.createAccount(account2);
        accountService.createAccount(account3);

        // Then: all should be retrievable
        assertNotNull(accountService.getAccount(1L));
        assertNotNull(accountService.getAccount(2L));
        assertNotNull(accountService.getAccount(3L));

        assertEquals(account1.getFirstName(), accountService.getAccount(1L).getFirstName());
        assertEquals(account1.getLastName(), accountService.getAccount(1L).getLastName());
        assertEquals(account1.getBalance(), accountService.getAccount(1L).getBalance());
        assertEquals(account2.getFirstName(), accountService.getAccount(2L).getFirstName());
        assertEquals(account2.getLastName(), accountService.getAccount(2L).getLastName());
        assertEquals(account2.getBalance(), accountService.getAccount(2L).getBalance());
        assertEquals(account3.getFirstName(), accountService.getAccount(3L).getFirstName());
        assertEquals(account3.getLastName(), accountService.getAccount(3L).getLastName());
        assertEquals(account3.getBalance(), accountService.getAccount(3L).getBalance());
    }

    @Test
    void testSuccessfulTransfer() {
        // Given: valid source and target accounts with valid balances
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);
        Account source = new Account(sourceKey, "Pesho", "P", 1000.0);
        Account target = new Account(targetKey, "Misho", "M", 500.0);

        accountService.createAccount(source);
        accountService.createAccount(target);

        // When: a transfer occurs
        accountService.transfer(source, target, 200.0);

        // Then: both balances should be updated
        assertEquals(800.0, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(700.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferInsufficientBalance() {
        // Given source account has insufficient balance
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);
        Account source = new Account(sourceKey, "Gosho", "G", 100.0);
        Account target = new Account(targetKey, "Misho", "M", 500.0);

        accountService.createAccount(source);
        accountService.createAccount(target);

        // When trying to transfer more than available balance
        // Then should throw InsufficientBalanceException
        assertThrows(InsufficientBalanceException.class, () ->
                accountService.transfer(source, target, 200.0)
        );

        // And balances should remain unchanged
        assertEquals(100.0, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(500.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferToSameAccount() {
        // Given a single account
        AccountKey accountKey = AccountKey.valueOf(1);
        Account acc = new Account(accountKey, "Gosho", "G", 5500.0);
        accountService.createAccount(acc);

        // When attempting to transfer to the same account
        // Then should throw InvalidTransferException
        assertThrows(InvalidTransferException.class, () ->
                accountService.transfer(acc, acc, 100.0)
        );

        // And balance should remain unchanged
        assertEquals(5500.0, accountService.getAccount(1).getBalance(), 0.001);
    }

    @Test
    void testTransferNegativeAmount() {
        // Given two valid accounts
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);
        Account source = new Account(sourceKey, "Gosho", "G", 1000.0);
        Account target = new Account(targetKey, "Misho", "M", 2000.0);

        accountService.createAccount(source);
        accountService.createAccount(target);

        // When attempting to transfer negative amount
        // Then should throw InvalidTransferException
        assertThrows(InvalidTransferException.class, () ->
                accountService.transfer(source, target, -100.0)
        );

        // And balances should remain unchanged
        assertEquals(1000.0, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(2000.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferZeroAmount() {
        // Given two valid accounts
        AccountKey source = AccountKey.valueOf(1);
        AccountKey target = AccountKey.valueOf(2);
        Account acc1 = new Account(source, "Gosho", "G", 1000.0);
        Account acc2 = new Account(target, "Misho", "M", 2000.0);

        accountService.createAccount(acc1);
        accountService.createAccount(acc2);

        // When attempting to transfer a zero amount
        // Then should throw InvalidTransferException
        assertThrows(InvalidTransferException.class, () ->
                accountService.transfer(acc1, acc2, 0.0)
        );

        // And balances should remain unchanged
        assertEquals(1000.0, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(2000.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferSourceNotFound() {
        // Given only target account exists
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);
        Account source = new Account(sourceKey, "Gosho", "G", 1000.0);
        Account target = new Account(targetKey, "Misho", "M", 500.0);

        accountService.createAccount(target);

        // When attempting transfer from non-existent source
        // Then should throw AccountNotFoundException
        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(source, target, 100.0)
        );

        // And balances should remain unchanged
        assertEquals(500.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferTargetNotFound() {
        // Given only source account exists
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);
        Account source = new Account(sourceKey, "Gosho", "G", 1000.0);
        Account target = new Account(targetKey, "Misho", "M", 500.0);

        accountService.createAccount(source);

        // When attempting transfer to non-existent target
        // Then should throw AccountNotFoundException
        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(source, target, 100.0)
        );

        // And balances should remain unchanged
        assertEquals(1000.0, accountService.getAccount(1).getBalance(), 0.001);
    }

    @Test
    void testTransferSourceNull() {
        // Given the source account is null
        AccountKey targetKey = AccountKey.valueOf(2);
        Account target = new Account(targetKey, "Misho", "M", 500.0);
        accountService.createAccount(target);

        // When attempting transfer funds
        // Then should throw InvalidTransferException
        assertThrows(InvalidTransferException.class, () ->
                accountService.transfer(null, target, 100.0)
        );

        // And balances should remain unchanged
        assertEquals(500.0, accountService.getAccount(2).getBalance(), 0.001);
    }

    @Test
    void testTransferTargetNull() {
        // Given the target account is null
        AccountKey sourceKey = AccountKey.valueOf(1);
        Account source = new Account(sourceKey, "Gosho", "G", 1000.0);
        accountService.createAccount(source);

        // When attempting transfer funds
        // Then should throw InvalidTransferException
        assertThrows(InvalidTransferException.class, () ->
                accountService.transfer(source, null, 100.0)
        );

        // And balances should remain unchanged
        assertEquals(1000.0, accountService.getAccount(1).getBalance(), 0.001);
    }

    @Test
    void testTransferExactBalance() {
        // Given source account has exact amount to transfer
        AccountKey source = AccountKey.valueOf(1);
        AccountKey target = AccountKey.valueOf(2);
        Account acc1 = new Account(source, "Gosho", "G", 100.0);
        Account acc2 = new Account(target, "Misho", "M", 500.0);

        accountService.createAccount(acc1);
        accountService.createAccount(acc2);

        // When transferring exact balance
        accountService.transfer(acc1, acc2, 100.0);

        // Then the source should have zero balance and target account's balance should be updated
        assertEquals(0.0, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(600.0, accountService.getAccount(2).getBalance(), 0.001);
    }
}