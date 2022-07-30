package io.github.dankoller.antifraud.service;

import io.github.dankoller.antifraud.entity.Card;
import io.github.dankoller.antifraud.entity.transaction.Transaction;
import io.github.dankoller.antifraud.entity.transaction.TransactionResult;
import io.github.dankoller.antifraud.persistence.CardRepository;
import io.github.dankoller.antifraud.persistence.TransactionRepository;
import io.github.dankoller.antifraud.util.CardValidator;
import io.github.dankoller.antifraud.util.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unused")
public class TransactionService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionValidator transactionValidator;

    /**
     * Processes a transaction by validating card number and ip address.
     *
     * @param transaction Transaction object to be processed
     * @return Map with the result and information of the transaction
     */
    public Map<String, String> processTransaction(Transaction transaction) {
        Long amount = transaction.getAmount();
        String ip = transaction.getIp();
        String cardNumber = transaction.getNumber();

        // Transferring negative amounts aren't allowed
        if (amount == null || ip == null || cardNumber == null || amount <= 0 || ip.isEmpty() || cardNumber.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transaction");
        }

        // Check if the card already exists in the database
        saveCardIfNotExists(cardNumber);

        // Verify transaction and save it in the database
        transactionValidator.verifyTransaction(transaction);
        transactionRepository.save(transaction);

        return Map.of(
                "result", transaction.getResult(),
                "info", transaction.getInfo()
        );
    }

    /**
     * Helper method to save a card in the database if it doesn't exist.
     *
     * @param cardNumber The card number to be checked and saved
     */
    private void saveCardIfNotExists(String cardNumber) {
        if (cardRepository.findByNumber(cardNumber).isEmpty()) {
            cardRepository.save(new Card(cardNumber, false));
        }
    }

    /**
     * Provide feedback for potential fraudulent transactions. To be used by support team.
     *
     * @param transactionId The id of the transaction to be checked
     * @param feedback      The feedback to be provided
     * @return Transaction object with the feedback provided
     */
    public Transaction updateTransaction(long transactionId, String feedback) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (TransactionValidator.isFeedbackWrongFormat(feedback)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid feedback");
        } else if (!transaction.getFeedback().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction already processed");
        } else if (transaction.getResult().equals(feedback)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Transaction not processable");
        }

        changeLimit(transaction, feedback);

        transaction.setFeedback(TransactionResult.valueOf(feedback));
        transactionRepository.save(transaction);

        return transaction;
    }

    /**
     * Helper method to change the limit for a certain transaction based on the provided feedback.
     *
     * @param transaction The transaction to be checked
     * @param feedback    The feedback to be provided
     */
    private void changeLimit(Transaction transaction, String feedback) {
        String trResult = transaction.getResult();
        Card card = cardRepository.findByNumber(transaction.getNumber())
                .orElseThrow(AssertionError::new);

        // Formula for increasing the limit: new_limit = 0.8 * current_limit + 0.2 * value_from_transaction
        int increasedAllowed = (int) Math.ceil(0.8 * card.getAllowedLimit() + 0.2 * transaction.getAmount());
        int decreasedAllowed = (int) Math.ceil(0.8 * card.getAllowedLimit() - 0.2 * transaction.getAmount());
        int increasedManual = (int) Math.ceil(0.8 * card.getManualLimit() + 0.2 * transaction.getAmount());
        int decreasedManual = (int) Math.ceil(0.8 * card.getManualLimit() - 0.2 * transaction.getAmount());

        // Set the new limit based on the feedback
        if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("ALLOWED")) {
            card.setAllowedLimit(decreasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("ALLOWED")) {
            card.setAllowedLimit(decreasedAllowed);
            card.setManualLimit(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setAllowedLimit(increasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setManualLimit(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("PROHIBITED")) {
            card.setAllowedLimit(increasedAllowed);
            card.setManualLimit(increasedManual);
        } else if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("PROHIBITED")) {
            card.setManualLimit(increasedManual);
        }

        // Save the new limit in the database
        cardRepository.save(card);
    }

    /**
     * Return transaction history for a given card number
     *
     * @param cardNumber The card number to be checked
     * @return List of transaction history for the given card number
     */
    public List<Transaction> getTransactionHistory(String cardNumber) {
        if (CardValidator.isNonValid(cardNumber)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        List<Transaction> transactions = transactionRepository.findAllByNumber(cardNumber);
        if (transactions.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return transactions;
    }
}
