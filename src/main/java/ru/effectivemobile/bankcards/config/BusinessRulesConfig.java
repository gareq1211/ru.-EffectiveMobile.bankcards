package ru.effectivemobile.bankcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "business.rules")
@Validated
public class BusinessRulesConfig {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minCardBalance = new BigDecimal("0.00");

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal maxTransferAmount = new BigDecimal("1000000.00");

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal minTransferAmount = new BigDecimal("0.01");

    @Min(0)
    private int maxCardsPerUser = 5;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minInitialBalance = new BigDecimal("0.00");

      public BigDecimal getMinCardBalance() {
        return minCardBalance;
    }

    public void setMinCardBalance(BigDecimal minCardBalance) {
        this.minCardBalance = minCardBalance;
    }

    public BigDecimal getMaxTransferAmount() {
        return maxTransferAmount;
    }

    public void setMaxTransferAmount(BigDecimal maxTransferAmount) {
        this.maxTransferAmount = maxTransferAmount;
    }

    public BigDecimal getMinTransferAmount() {
        return minTransferAmount;
    }

    public void setMinTransferAmount(BigDecimal minTransferAmount) {
        this.minTransferAmount = minTransferAmount;
    }

    public int getMaxCardsPerUser() {
        return maxCardsPerUser;
    }

    public void setMaxCardsPerUser(int maxCardsPerUser) {
        this.maxCardsPerUser = maxCardsPerUser;
    }

    public BigDecimal getMinInitialBalance() {
        return minInitialBalance;
    }

    public void setMinInitialBalance(BigDecimal minInitialBalance) {
        this.minInitialBalance = minInitialBalance;
    }
}