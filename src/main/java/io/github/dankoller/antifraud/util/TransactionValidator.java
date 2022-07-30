package io.github.dankoller.antifraud.util;

import io.github.dankoller.antifraud.entity.Card;
import io.github.dankoller.antifraud.entity.transaction.Transaction;
import io.github.dankoller.antifraud.entity.transaction.TransactionResult;
import io.github.dankoller.antifraud.persistence.CardRepository;
import io.github.dankoller.antifraud.persistence.SuspiciousIPRepository;
import io.github.dankoller.antifraud.persistence.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@SuppressWarnings("unused")
public class TransactionValidator {

    private Transaction transaction;

    @Autowired
    private SuspiciousIPRepository suspiciousIPRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Set<String> info = new TreeSet<>();

    /**
     * Checks if the feedback matches the transaction result.
     *
     * @param feedback Feedback to check
     * @return True if the feedback matches the transaction result, false otherwise.
     */
    public static boolean isFeedbackWrongFormat(String feedback) {
        return Arrays.stream(TransactionResult.values()).noneMatch(result -> result.name().equals(feedback));
    }

    /**
     * Receives a new transaction and verifies it based on certain heuristics (e.g. suspicious IPs, stolen cards, etc.).
     *
     * @param transaction Transaction to verify.
     */
    public void verifyTransaction(Transaction transaction) {
        this.transaction = transaction;
        this.info = new TreeSet<>();

        transaction.setResult(TransactionResult.ALLOWED);

        // Heuristics for the transaction
        checkIfStolenCard();
        checkIfSuspiciousIP();
        checkIfCorrelationExists();
        checkIfAmountIsTooHigh();

        transaction.setInfo(formatInfo());
    }

    /**
     * Checks if the card number of the transaction is flagged as stolen in the database.
     */
    private void checkIfStolenCard() {
        if (cardRepository.existsByNumberAndIsLockedTrue(transaction.getNumber())) {
            transaction.setResult(TransactionResult.PROHIBITED);
            info.add("card-number");
        }
    }

    /**
     * Checks if the IP address of the transaction is flagged as suspicious in the database.
     */
    private void checkIfSuspiciousIP() {
        if (suspiciousIPRepository.findByIp(transaction.getIp()).isPresent()) {
            transaction.setResult(TransactionResult.PROHIBITED);
            info.add("ip");
        }
    }

    /**
     * Checks if the transaction is correlated with another transaction based on the region and ip address.
     */
    private void checkIfCorrelationExists() {
        List<Transaction> timeBeforeTransaction = transactionRepository.findAllByDateBetweenAndNumber(
                transaction.getDate().minusHours(1),
                transaction.getDate(),
                transaction.getNumber()
        );

        long regionCount = timeBeforeTransaction.stream()
                .map(Transaction::getRegion)
                .filter(region -> !Objects.equals(region, transaction.getRegion()))
                .distinct().count();

        long ipCount = timeBeforeTransaction.stream()
                .map(Transaction::getIp)
                .filter(ip -> !Objects.equals(ip, transaction.getIp()))
                .distinct().count();

        if (regionCount == 2 && !Objects.equals(transaction.getResult(), TransactionResult.PROHIBITED.name())) {
            transaction.setResult(TransactionResult.MANUAL_PROCESSING);
            info.add("region-correlation");
        }

        if (ipCount == 2 && !Objects.equals(transaction.getResult(), TransactionResult.PROHIBITED.name())) {
            transaction.setResult(TransactionResult.MANUAL_PROCESSING);
            info.add("ip-correlation");
        }

        if (regionCount > 2) {
            transaction.setResult(TransactionResult.PROHIBITED);
            info.add("region-correlation");
        }

        if (ipCount > 2) {
            transaction.setResult(TransactionResult.PROHIBITED);
            info.add("ip-correlation");
        }
    }

    /**
     * Checks if the amount of the transaction is too high based on the limit of the customers card.
     */
    private void checkIfAmountIsTooHigh() {
        Card card = cardRepository.findByNumber(transaction.getNumber()).orElseThrow(AssertionError::new);

        int allowedLimit = card.getAllowedLimit();
        int manualLimit = card.getManualLimit();

        if (transaction.getAmount() > allowedLimit && transaction.getAmount() <= manualLimit
                && !Objects.equals(transaction.getResult(), TransactionResult.PROHIBITED.name())) {

            transaction.setResult(TransactionResult.MANUAL_PROCESSING);
            info.add("amount");
        }

        if (transaction.getAmount() > manualLimit) {
            if (!Objects.equals(transaction.getResult(), TransactionResult.PROHIBITED.name())) {
                info.clear();
            }
            info.add("amount");
            transaction.setResult(TransactionResult.PROHIBITED);
        }
    }

    /**
     * Helper method that formats the info of the transaction.
     *
     * @return Formatted info of the transaction.
     */
    private String formatInfo() {
        if (Objects.equals(transaction.getResult(), TransactionResult.ALLOWED.name())) {
            info.add("none");
        }

        return String.join(", ", info);
    }
}
