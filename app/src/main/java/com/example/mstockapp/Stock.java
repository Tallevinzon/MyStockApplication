package com.example.mstockapp;

public class Stock {
    private String symbol;
    private String price;
    private String dailyChange;

    private Stock(StockBuilder builder) {
        this.symbol = builder.symbol;
        this.price = builder.price;
        this.dailyChange = builder.dailyChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getDailyChange() {
        return dailyChange;
    }

    public static class StockBuilder {
        private String symbol;
        private String price;
        private String dailyChange;

        public StockBuilder setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public StockBuilder setPrice(String price) {
            this.price = price;
            return this;
        }

        public StockBuilder setDailyChange(String dailyChange) {
            this.dailyChange = dailyChange;
            return this;
        }

        public Stock build() {
            return new Stock(this);
        }
    }
}
