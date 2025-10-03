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

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        // Then should throw NullPointerException
        assertThrows(NullPointerException.class, () ->
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
        // Then should throw NullPointerException
        assertThrows(NullPointerException.class, () ->
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

    @Test
    void testConcurrentTransfersFromSameAccount() throws InterruptedException {
        // Given account with $1000
        AccountKey source = AccountKey.valueOf(1);
        AccountKey target1 = AccountKey.valueOf(2);
        AccountKey target2 = AccountKey.valueOf(3);

        Account sourceAcc = new Account(source, "Pesho", "P", 1000.0);
        Account targetAcc1 = new Account(target1, "Gosho", "G", 0.0);
        Account targetAcc2 = new Account(target2, "Misho", "M", 0.0);

        accountService.createAccount(sourceAcc);
        accountService.createAccount(targetAcc1);
        accountService.createAccount(targetAcc2);

        int numThreads = 10;
        Double transferAmount = 50.0;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        // When multiple threads transfer from same account
        for (int i = 0; i < numThreads / 2; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    accountService.transfer(sourceAcc, targetAcc1, transferAmount);
                } catch (Exception e) {
                    fail("Should not throw exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();

            new Thread(() -> {
                try {
                    startLatch.await();
                    accountService.transfer(sourceAcc, targetAcc2, transferAmount);
                } catch (Exception e) {
                    fail("Should not throw exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // Start all threads simultaneously
        doneLatch.await(5, TimeUnit.SECONDS);

        // Then total money should be conserved
        Double totalBalance = accountService.getAccount(1).getBalance() +
                accountService.getAccount(2).getBalance() +
                accountService.getAccount(3).getBalance();

        assertEquals(1000.0, totalBalance, 0.001);
    }

    @Test
    void testDeadlockPrevention() throws InterruptedException {
        // Given two accounts
        AccountKey accountAKey = AccountKey.valueOf(1);
        AccountKey accountBKey = AccountKey.valueOf(2);

        Account accA = new Account(accountAKey, "Pesho", "P", 1000.0);
        Account accB = new Account(accountBKey, "Gosho", "G", 1000.0);

        accountService.createAccount(accA);
        accountService.createAccount(accB);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        // Thread 1: A → B
        new Thread(() -> {
            try {
                startLatch.await();
                accountService.transfer(accA, accB, 100.0);
                successCount.incrementAndGet();
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        }).start();

        // Thread 2: B → A (potential deadlock if not handled properly)
        new Thread(() -> {
            try {
                startLatch.await();
                accountService.transfer(accB, accA, 100.0);
                successCount.incrementAndGet();
            } catch (Exception e) {
                // May fail
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        boolean completed = doneLatch.await(3, TimeUnit.SECONDS);

        // Then should complete without deadlock
        assertTrue(completed, "Transfers should complete without deadlock");
        assertEquals(2, successCount.get(), "Both transfers should succeed");
    }

    @Test
    void testMoneyConservation() throws InterruptedException {
        // Given 10 accounts
        int numAccounts = 10;
        Double initialBalance = 5000.0;
        Double totalMoney = numAccounts * initialBalance;

        for (int i = 1; i <= numAccounts; i++) {
            AccountKey key = AccountKey.valueOf(i);
            Account acc = new Account(key, "Pesho" + i, "P" + i, initialBalance);
            accountService.createAccount(acc);
        }

        int numTransfers = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numTransfers);
        Random random = new Random();

        // When performing many random transfers concurrently
        for (int i = 0; i < numTransfers; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();

                    int sourceId = random.nextInt(numAccounts) + 1;
                    int targetId = random.nextInt(numAccounts) + 1;

                    if (sourceId != targetId) {
                        accountService.transfer(
                                accountService.getAccount(sourceId),
                                accountService.getAccount(targetId),
                                50.0
                        );
                    }
                } catch (Exception e) {
                    fail("Should not throw exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        // Then total money in system should remain the same
        Double finalTotal = 0.0;
        for (int i = 1; i <= numAccounts; i++) {
            finalTotal += accountService.getAccount(i).getBalance();
        }

        assertEquals(totalMoney, finalTotal, 0.001);
    }

    @Test
    void testConcurrentModificationStressTest() throws InterruptedException {
        // Given an account with a large balance
        AccountKey sourceKey = AccountKey.valueOf(1);
        AccountKey targetKey = AccountKey.valueOf(2);

        Account sourceAcc = new Account(sourceKey, "Pesho", "P", 100000.0);
        Account targetAcc = new Account(targetKey, "Gosho", "G", 0.0);

        accountService.createAccount(sourceAcc);
        accountService.createAccount(targetAcc);

        int numThreads = 1000;
        Double transferAmount = 1.0;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        // When A LOT of threads transfer from the same account a small amount
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    accountService.transfer(sourceAcc, targetAcc, transferAmount);
                } catch (Exception e) {
                    fail("Should not throw exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        Double expectedSource = 100000.0 - (numThreads * transferAmount);
        Double expectedTarget = numThreads * transferAmount;

        // Then with proper locking: source = 99000, target = 1000
        assertEquals(expectedSource, accountService.getAccount(1).getBalance(), 0.001);
        assertEquals(expectedTarget, accountService.getAccount(2).getBalance(), 0.001);
    }
}