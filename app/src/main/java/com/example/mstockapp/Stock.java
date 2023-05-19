package com.example.mstockapp;

public class Stock {
    String symbol;
    String price;
    String dailyChange;

    public Stock(String symbol, String price, String dailyChange){
        this.symbol = symbol;
        this.price = price;
        this.dailyChange = dailyChange;
    }
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDailyChange() {
        return dailyChange;
    }

    public void setDailyChange(String dailyChange) {
        this.dailyChange = dailyChange;
    }

}
