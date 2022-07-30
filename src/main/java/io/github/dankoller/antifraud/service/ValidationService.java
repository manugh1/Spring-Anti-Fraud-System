package io.github.dankoller.antifraud.service;

import io.github.dankoller.antifraud.entity.Card;
import io.github.dankoller.antifraud.entity.IPAddress;
import io.github.dankoller.antifraud.persistence.CardRepository;
import io.github.dankoller.antifraud.persistence.SuspiciousIPRepository;
import io.github.dankoller.antifraud.util.CardValidator;
import io.github.dankoller.antifraud.util.IPAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings("unused")
public class ValidationService {

    @Autowired
    private SuspiciousIPRepository suspiciousIPRepository;

    @Autowired
    private CardRepository cardRepository;

    /**
     * Save suspicious IP addresses to prevented further transactions.
     *
     * @param ip IP address to be saved
     * @return Saved IP address
     */
    public IPAddress saveSuspiciousIP(String ip) {
        if (IPAddressValidator.isNonValidIp(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address");
        }

        if (suspiciousIPRepository.findByIp(ip).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card number is flagged as stolen");
        }

        IPAddress ipAddress = new IPAddress(ip);
        suspiciousIPRepository.save(ipAddress);

        return ipAddress;
    }

    /**
     * Returns a list of all suspicious IP addresses currently stored.
     *
     * @return List of suspicious IP addresses
     */
    public Iterable<IPAddress> getSuspiciousIPs() {
        return suspiciousIPRepository.findAll();
    }

    /**
     * Remove a suspicious IP address from the database.
     *
     * @param ip IP address to be removed
     * @return A String containing the success status and the removed IP address
     */
    public String deleteSuspiciousIP(String ip) {
        if (IPAddressValidator.isNonValidIp(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address");
        }

        IPAddress ipAddress = suspiciousIPRepository.findByIp(ip)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "IP address not found"));

        suspiciousIPRepository.delete(ipAddress);

        return "IP " + ip + " successfully removed!";
    }

    /**
     * Save potentially stolen cards to prevent further transactions and reduce the risk of fraud for the customer.
     *
     * @param cardNumber The card number to be saved
     * @return Saved card entity
     */
    public Card saveStolenCard(String cardNumber) {
        if (CardValidator.isNonValid(cardNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card number");
        }

        Card card;

        // Check if the card already exists in the database
        if (cardRepository.findByNumber(cardNumber).isPresent()) {
            if (cardRepository.existsByNumberAndIsLockedTrue(cardNumber)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Card number is flagged as stolen");
            } else {
                // Lock the existing card to prevent fraud attempts
                card = cardRepository.findByNumber(cardNumber).get();
                card.setLocked(true);
            }
        } else {
            // Create a new card entity and lock it right away
            card = new Card(cardNumber, true);
        }

        cardRepository.save(card);

        return card;
    }

    /**
     * Remove a given card from the database of potentially stolen cards.
     *
     * @param cardNumber The card number to be removed
     * @return A String containing the success status and the removed card number
     */
    public String deleteStolenCard(String cardNumber) {
        if (CardValidator.isNonValid(cardNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card number");
        }

        Card card = cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card number not found"));

        if (card.isLocked()) cardRepository.delete(card);

        return "Card " + cardNumber + " successfully removed!";
    }

    /**
     * Returns a list of all potentially stolen cards currently stored.
     *
     * @return List of potentially stolen cards
     */
    public Iterable<Card> getStolenCards() {
        return cardRepository.findAllByIsLockedTrue();
    }
}
