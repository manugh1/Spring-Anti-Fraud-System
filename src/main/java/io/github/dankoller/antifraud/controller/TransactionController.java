package io.github.dankoller.antifraud.controller;

import io.github.dankoller.antifraud.entity.transaction.Transaction;
import io.github.dankoller.antifraud.persistence.TransactionRepository;
import io.github.dankoller.antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * This is the controller for the transaction endpoints.
 * It receives requests for certain operations and passes them to the service layer.
 * The results of the operations are returned to the client.
 * 'Unused fields' warnings are suppressed because the fields are automatically filled at runtime.
 */

@RestController
@RequestMapping("/api/antifraud")
@SuppressWarnings("unused")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get a list of all transactions.
     *
     * @return List of Transaction objects
     */
    @PostMapping(value = "/transaction", consumes = "application/json")
    public ResponseEntity<?> validateTransaction(@RequestBody Transaction transaction) {
        Map<String, String> response = transactionService.processTransaction(transaction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * An authenticated support user can provide feedback on a transaction.
     *
     * @return ResponseEntity containing the transaction's feedback
     */
    @PutMapping(value = "/transaction", consumes = "application/json")
    public ResponseEntity<?> provideFeedback(@RequestBody Map<String, String> transactionFeedback) {
        long transactionId = Long.parseLong(transactionFeedback.get("transactionId"));
        String feedback = transactionFeedback.get("feedback");

        Transaction transaction = transactionService.updateTransaction(transactionId, feedback);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    /**
     * Get a list of all transactions.
     *
     * @return List of Transaction objects
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return new ResponseEntity<>(transactionRepository.findAll(), HttpStatus.OK);
    }

    /**
     * Get a list of all transactions for a specific card number.
     *
     * @return List of Transaction objects for that card number
     */
    @GetMapping("/history/{number}")
    public ResponseEntity<?> getHistoryForCardNumber(@PathVariable String number) {
        List<Transaction> transactions = transactionService.getTransactionHistory(number);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}
