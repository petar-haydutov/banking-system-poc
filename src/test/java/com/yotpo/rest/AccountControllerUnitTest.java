package com.yotpo.rest;

import com.yotpo.account.Account;
import com.yotpo.account.AccountKey;
import com.yotpo.exception.AccountNotFoundException;
import com.yotpo.exception.InsufficientBalanceException;
import com.yotpo.exception.InvalidTransferException;
import com.yotpo.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AccountControllerUnitTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account(AccountKey.valueOf(1L), "Pesho", "Peshov", 1000.0);
        targetAccount = new Account(AccountKey.valueOf(2L), "Gosho", "Goshov", 500.0);
    }

    @Test
    void transfer_Success_Returns200() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(sourceAccount);
        when(accountService.getAccount(2L)).thenReturn(targetAccount);
        doNothing().when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 100.0);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService).getAccount(1L);
        verify(accountService).getAccount(2L);
        verify(accountService).transfer(sourceAccount, targetAccount, 100.0);
    }

    @Test
    void transfer_NegativeAmount_Returns400() {
        // Arrange
        when(accountService.getAccount(anyLong())).thenReturn(sourceAccount);
        when(accountService.getAccount(anyLong())).thenReturn(targetAccount);
        doThrow(new InvalidTransferException("Transfer amount must be positive"))
                .when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, -100.0);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountService).transfer(any(Account.class), any(Account.class), anyDouble());
    }

    @Test
    void transfer_ZeroAmount_Returns400() {
        // Arrange
        when(accountService.getAccount(anyLong())).thenReturn(sourceAccount);
        when(accountService.getAccount(anyLong())).thenReturn(targetAccount);
        doThrow(new InvalidTransferException("Transfer amount must be positive"))
                .when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 0.0);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountService).transfer(any(Account.class), any(Account.class), anyDouble());
    }

    @Test
    void transfer_SourceAccountNotFound_Returns404() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 100.0);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService).getAccount(1L);
        verify(accountService, never()).getAccount(2L);
        verify(accountService, never()).transfer(any(), any(), anyDouble());
    }

    @Test
    void transfer_TargetAccountNotFound_Returns404() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(sourceAccount);
        when(accountService.getAccount(2L)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 100.0);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService).getAccount(1L);
        verify(accountService).getAccount(2L);
        verify(accountService, never()).transfer(any(), any(), anyDouble());
    }

    @Test
    void transfer_InsufficientBalance_Returns500() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(sourceAccount);
        when(accountService.getAccount(2L)).thenReturn(targetAccount);
        doThrow(new InsufficientBalanceException(sourceAccount.getAccountKey()))
                .when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 2000.0);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(accountService).transfer(sourceAccount, targetAccount, 2000.0);
    }

    @Test
    void transfer_InvalidTransferException_Returns400() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(sourceAccount);
        when(accountService.getAccount(2L)).thenReturn(targetAccount);
        doThrow(new InvalidTransferException("Cannot transfer to the same account"))
                .when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 100.0);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountService).transfer(sourceAccount, targetAccount, 100.0);
    }

    @Test
    void transfer_AccountNotFoundExceptionDuringTransfer_Returns404() {
        // Arrange - simulates race condition where account is removed after initial check
        when(accountService.getAccount(1L)).thenReturn(sourceAccount);
        when(accountService.getAccount(2L)).thenReturn(targetAccount);
        doThrow(new AccountNotFoundException(sourceAccount.getAccountKey()))
                .when(accountService).transfer(any(Account.class), any(Account.class), anyDouble());

        // Act
        ResponseEntity<Void> response = accountController.transfer(1L, 2L, 100.0);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService).transfer(sourceAccount, targetAccount, 100.0);
    }
}
