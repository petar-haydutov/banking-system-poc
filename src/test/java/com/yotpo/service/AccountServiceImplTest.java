package com.yotpo.service;

import com.yotpo.account.Account;
import com.yotpo.account.AccountKey;
import com.yotpo.exception.AccountAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountServiceImpl();
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
}