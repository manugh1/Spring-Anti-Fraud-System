package io.github.dankoller.antifraud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column
    private String number;

    @JsonIgnore
    private boolean isLocked;

    @JsonIgnore
    private int allowedLimit = 200;

    @JsonIgnore
    private int manualLimit = 1500;

    public Card(String number, boolean isLocked) {
        this.number = number;
        this.isLocked = isLocked;
    }
}
