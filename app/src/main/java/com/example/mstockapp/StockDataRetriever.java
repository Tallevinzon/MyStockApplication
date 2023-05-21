package com.example.mstockapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockDataRetriever {
    private static final String TAG = StockDataRetriever.class.getSimpleName();
    private static final String BASE_URL = "https://cloud.iexapis.com/stable/stock/%s/quote?token=sk_ae7d7b54f1eb44268f556d14736d265f";

    public interface StockDataListener {
        void onStockDataReceived(String symbol, double latestPrice, double dailyChange);
        void onStockDataError(String error);
    }

    public static void getStockData(String symbol, StockDataListener listener) {
        OkHttpClient client = new OkHttpClient();

        String url = String.format(BASE_URL, symbol);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onStockDataError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        double latestPrice = json.getDouble("latestPrice");
                        double dailyChange = json.getDouble("change");
                        listener.onStockDataReceived(symbol, latestPrice, dailyChange);
                    } catch (JSONException e) {
                        listener.onStockDataError(e.getMessage());
                    }
                } else {
                    listener.onStockDataError("Failed to retrieve stock data");
                }
            }
        });
    }
}