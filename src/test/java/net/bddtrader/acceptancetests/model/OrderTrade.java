package net.bddtrader.acceptancetests.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.bddtrader.portfolios.TradeType;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OrderTrade {

    private Integer amount;
    private String securityCode;
    private TradeType type;
    private double priceInCents;


    @JsonCreator
    public OrderTrade(Integer amount, String securityCode, TradeType buyOrDeposit, double priceInCents){
        this.amount = amount;
        this.securityCode = securityCode;
        this.type = buyOrDeposit;
        this.priceInCents = priceInCents;

    }

    public Integer getAmount() {
        return amount;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public TradeType getType() {
        return type;
    }

    public double getPriceInCents() {
        return priceInCents;
    }
}
