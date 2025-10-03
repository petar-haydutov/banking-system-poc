package com.yotpo.rest;

import com.yotpo.account.Account;
import com.yotpo.account.AccountKey;
import com.yotpo.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService.clear();
    }

    @Test
    void transfer_Success_Returns200AndUpdatesBalances() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "250.0"))
                .andExpect(status().isOk());

        // Assert
        assertEquals(750.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(750.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_NegativeAmount_Returns400() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "-100.0"))
                .andExpect(status().isBadRequest());

        // Verify balances unchanged
        assertEquals(1000.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(500.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_ZeroAmount_Returns400() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "0"))
                .andExpect(status().isBadRequest());

        // Verify balances unchanged
        assertEquals(1000.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(500.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_AmountHasTooManyDecimals_Returns400() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "20.555"))
                .andExpect(status().isBadRequest());

        // Verify balances unchanged
        assertEquals(1000.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(500.0, accountService.getAccount(2L).getBalance(), 0.01);
    }


    @Test
    void transfer_MissingSourceId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("target_id", "2")
                        .param("amount", "100.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_MissingTargetId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("amount", "100.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_MissingAmount_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_InvalidAmountFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_SourceAccountNotFound_Returns404() throws Exception {
        // Arrange
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "999")
                        .param("target_id", "2")
                        .param("amount", "100.0"))
                .andExpect(status().isNotFound());

        // Verify target balance unchanged
        assertEquals(500.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_TargetAccountNotFound_Returns404() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        accountService.createAccount(source);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "999")
                        .param("amount", "100.0"))
                .andExpect(status().isNotFound());

        // Verify source balance unchanged
        assertEquals(1000.0, accountService.getAccount(1L).getBalance(), 0.01);
    }

    @Test
    void transfer_InsufficientBalance_Returns500() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 100.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "200.0"))
                .andExpect(status().isInternalServerError());

        // Verify balances unchanged
        assertEquals(100.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(500.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_ExactBalance_Success() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 100.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 500.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act & Assert
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "100.0"))
                .andExpect(status().isOk());

        // Verify balances
        assertEquals(0.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(600.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_MultipleSequentialTransfers_Success() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 1000.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 0.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act - Multiple transfers
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "100.0"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "200.0"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "300.0"))
                .andExpect(status().isOk());

        // Assert
        assertEquals(400.0, accountService.getAccount(1L).getBalance(), 0.01);
        assertEquals(600.0, accountService.getAccount(2L).getBalance(), 0.01);
    }

    @Test
    void transfer_SmallDecimalAmount_Success() throws Exception {
        // Arrange
        Account source = new Account(AccountKey.valueOf(1L), "Pesho", "P", 100.0);
        Account target = new Account(AccountKey.valueOf(2L), "Misho", "M", 0.0);
        accountService.createAccount(source);
        accountService.createAccount(target);

        // Act
        mockMvc.perform(post("/api/operations/transfer")
                        .param("source_id", "1")
                        .param("target_id", "2")
                        .param("amount", "0.01"))
                .andExpect(status().isOk());

        // Assert
        assertEquals(99.99, accountService.getAccount(1L).getBalance());
        assertEquals(0.01, accountService.getAccount(2L).getBalance());
    }
}
