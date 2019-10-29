package net.bddtrader.portfolios;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.bddtrader.tradingdata.PriceReader;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Positions {




        private Map<String, Position> positionsBySecurity = new HashMap<>();

        public Positions(){
            super();
        }

        @JsonCreator
        public Positions(final Map<String, Position> positionsBySecurity){
            this.positionsBySecurity = positionsBySecurity;
        }

        public void apply(Trade trade) {
            if (positionsBySecurity.containsKey(trade.getSecurityCode())) {
                positionsBySecurity.put(trade.getSecurityCode(),
                                        positionsBySecurity.get(trade.getSecurityCode()).apply(trade));
            } else {
                positionsBySecurity.put(trade.getSecurityCode(), Position.fromTrade(trade));
            }
        }

        public Map<String,Position> getPositions() {
            return new HashMap<>(positionsBySecurity);
        }

        public void updateMarketPricesUsing(PriceReader priceReader) {
            positionsBySecurity.keySet().forEach(
                    securityCode -> {
                        Double marketPrice = priceReader.getPriceFor(securityCode);
                        positionsBySecurity.put(securityCode,
                                                positionsBySecurity.get(securityCode).withMarketPriceOf(marketPrice));
                    }
            );
        }
    }