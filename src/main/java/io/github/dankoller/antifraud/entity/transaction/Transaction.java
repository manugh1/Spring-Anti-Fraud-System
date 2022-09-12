package io.github.dankoller.antifraud.entity.transaction;

import io.github.dankoller.antifraud.entity.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long amount;
    private String number;
    private String ip;

    @Enumerated(EnumType.STRING)
    public Region region;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private TransactionResult result;

    @JsonIgnore
    @Transient
    private String info;

    @JsonIgnore
    private TransactionResult feedback;

    // Custom JSON properties
    @JsonProperty("transactionId")
    public Long getId() {
        return id;
    }

    @JsonProperty("result")
    public String getResult() {
        return result.name();
    }

    @JsonProperty("feedback")
    public String getFeedback() {
        return feedback == null ? "" : feedback.name();
    }
}
