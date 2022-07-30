package io.github.dankoller.antifraud.controller;

import io.github.dankoller.antifraud.entity.Card;
import io.github.dankoller.antifraud.entity.IPAddress;
import io.github.dankoller.antifraud.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * This is the controller for the validation and administration endpoints.
 * It receives requests for certain operations and passes them to the service layer.
 * The results of the operations are returned to the client.
 * 'Unused fields' warnings are suppressed because the fields are automatically filled at runtime.
 */

@RestController
@RequestMapping("/api/antifraud")
@SuppressWarnings("unused")
public class ValidationController {

    @Autowired
    private ValidationService validationService;

    /**
     * Save suspicious IP addresses to prevent them from being used in future transactions.
     *
     * @param ipAsJson JSON string containing the IP address
     * @return ResponseEntity containing the IP address
     */
    @PostMapping("/suspicious-ip")
    public ResponseEntity<?> saveSuspiciousIP(@Valid @RequestBody Map<String, String> ipAsJson) {
        String ip = ipAsJson.get("ip");
        IPAddress ipAddress = validationService.saveSuspiciousIP(ip);

        return new ResponseEntity<>(ipAddress, HttpStatus.OK);
    }

    /**
     * Return a list of all currently suspicious (and therefore blocked) IP addresses.
     *
     * @return List of blocked IPAddress objects
     */
    @GetMapping("/suspicious-ip")
    public ResponseEntity<?> getSuspiciousIPs() {
        return new ResponseEntity<>(validationService.getSuspiciousIPs(), HttpStatus.OK);
    }

    /**
     * Remove banned IP addresses from the list of suspicious IP addresses.
     *
     * @param ip JSON string containing the IP address to be unbanned
     * @return ResponseEntity containing the IP address
     */
    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<?> removeSuspiciousIP(@PathVariable String ip) {
        String status = validationService.deleteSuspiciousIP(ip);

        return new ResponseEntity<>(Map.of("status", status), HttpStatus.OK);
    }

    /**
     * Save suspicious cards to prevent them from being used in future transactions.
     *
     * @param cardAsJson JSON string containing the card number
     * @return ResponseEntity containing the card number
     */
    @PostMapping("/stolencard")
    public ResponseEntity<?> saveStolenCard(@Valid @RequestBody Map<String, String> cardAsJson) {
        String cardNumber = cardAsJson.get("number");
        Card card = validationService.saveStolenCard(cardNumber);

        return new ResponseEntity<>(card, HttpStatus.OK);
    }

    /**
     * Return a list of all currently banned cards.
     *
     * @return List of banned Card objects
     */
    @GetMapping("/stolencard")
    public ResponseEntity<?> getStolenCards() {
        return new ResponseEntity<>(validationService.getStolenCards(), HttpStatus.OK);
    }

    /**
     * Remove banned cards from the list of banned cards.
     *
     * @param cardNumber JSON string containing the card number to be unbanned
     * @return ResponseEntity containing the card number
     */
    @DeleteMapping("/stolencard/{cardNumber}")
    public ResponseEntity<?> removeStolenCard(@PathVariable String cardNumber) {
        String status = validationService.deleteStolenCard(cardNumber);

        return new ResponseEntity<>(Map.of("status", status), HttpStatus.OK);
    }
}
