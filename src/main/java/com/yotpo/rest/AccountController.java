package com.yotpo.rest;

import com.yotpo.account.Account;
import com.yotpo.exception.AccountNotFoundException;
import com.yotpo.exception.InsufficientBalanceException;
import com.yotpo.exception.InvalidTransferException;
import com.yotpo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations")
public class AccountController extends AbstractAccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Transfer money between accounts.
     *
     * Note: Exception handling is done locally in this method rather than using
     * a global @ControllerAdvice to maintain explicit control flow and simplify
     * unit testing. In a larger production system with many endpoints sharing
     * common exception handling patterns, a global exception handler would be
     * more appropriate.
     *
     * @param sourceId ID of the source account
     * @param targetId ID of the target account
     * @param amount Amount to transfer (must be positive)
     * @return 200 OK on success, 400 for validation errors, 404 if account not found,
     *      500 for insufficient balance
     */
    @Override
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestParam("source_id") long sourceId,
                                         @RequestParam("target_id") long targetId,
                                         @RequestParam("amount") double amount) {
        Account source = accountService.getAccount(sourceId);
        if (source == null) {
            return ResponseEntity.notFound().build();
        }

        Account target = accountService.getAccount(targetId);
        if (target == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            accountService.transfer(source, target, amount);
            return ResponseEntity.ok().build();
        } catch (InvalidTransferException e) {
            return ResponseEntity.badRequest().build();
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
