package com.example.mstockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import java.util.List;

        public class MainActivity extends AppCompatActivity implements StockListAdapter.AdapterCallback {
            private List<Stock> Stocks = new ArrayList<>();
            private List<Stock> refreshedArrayStocks = new ArrayList<>();
            private EditText symbolTxtInput;
            private ListView stockList;
            private StockListAdapter adapter;
            private Handler handler;
            private Runnable runnable;
            private boolean onStartUpFlag = false;
            private String jsonContent = "";

            // Observer pattern implementation
            private List<StockDataListener> stockDataListeners = new ArrayList<>();

            public interface StockDataListener {
                void onStockDataReceived(String symbol, double latestPrice, double dailyChange);
                void onStockDataError(String error);
            }

            public void addStockDataListener(StockDataListener listener) {
                stockDataListeners.add(listener);
            }

            public void removeStockDataListener(StockDataListener listener) {
                stockDataListeners.remove(listener);
            }

            public void notifyStockDataReceived(String symbol, double latestPrice, double dailyChange) {
                for (StockDataListener listener : stockDataListeners) {
                    listener.onStockDataReceived(symbol, latestPrice, dailyChange);
                }
            }

            public void notifyStockDataError(String error) {
                for (StockDataListener listener : stockDataListeners) {
                    listener.onStockDataError(error);
                }
            }

            // End of observer pattern implementation

            private StockDataRetriever.StockDataListener stockDataListener = new StockDataRetriever.StockDataListener() {
                @Override
                public void onStockDataReceived(String symbol, double latestPrice, double dailyChange) {
                    notifyStockDataReceived(symbol, latestPrice, dailyChange);
                }

                @Override
                public void onStockDataError(String error) {
                    notifyStockDataError(error);
                }
            };

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                Log.d(TAG, "onCreate: Started.");
                symbolTxtInput = (EditText) findViewById(R.id.symbolTxtInput);
                stockList = (ListView) findViewById(R.id.StockList);
                adapter = new StockListAdapter(this, R.layout.activity_list_view, Stocks);
                stockList.setAdapter(adapter);
                adapter.setCallback((StockListAdapter.AdapterCallback) this);

                // Observer pattern implementation
                addStockDataListener(new StockDataListener() {
                    @Override
                    public void onStockDataReceived(String symbol, double latestPrice, double dailyChange) {
                        // Handle the received stock data
                        Log.e(TAG, "Symbol: " + symbol + ", Latest Price: " + latestPrice + ", Daily Change: " + dailyChange);
                        Stock refreshedStock = new Stock(symbol, String.valueOf(latestPrice), String.valueOf(dailyChange));
                        refreshedArrayStocks.add(refreshedStock);

                        if (refreshedArrayStocks.size() == Stocks.size()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the values in the original Stocks ArrayList
                                    for (int j = 0; j < refreshedArrayStocks.size(); j++) {
                                        Stock refreshedStock = refreshedArrayStocks.get(j);
                                        Stocks.set(j, refreshedStock);
                                    }

                                    adapter.notifyDataSetChanged();

                                    // Convert the updated stock list to JSON and save it to a file
                                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                    String json = gson.toJson(Stocks);
                                    saveToLocalFile(json);
                                }
                            });
                        }
                    }

                    @Override
                    public void onStockDataError(String error) {
                        // Handle the error in retrieving stock data
                        Log.e(TAG, "Error: " + error);
                    }
                });

                try {
                    Context context = getApplicationContext();
                    File file = new File(context.getFilesDir(), "stockData.json"); // File path and name

                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    jsonContent = stringBuilder.toString();
                    reader.close();

                    // Do something with the JSON content
                } catch (IOException e) {
                    e.printStackTrace();
                    // Error occurred while reading the file
                }

                Gson gson = new Gson();
                Stock[] stocks = gson.fromJson(jsonContent, Stock[].class);

                onStartUpFlag = true;
                for (Stock stock : stocks) {
                    // Process each stock object
                    StockDataRetriever.getStockData(stock.getSymbol(), stockDataListener);
                    Stocks.add(stock);
                    adapter.notifyDataSetChanged();
                }

                // Initialize the Handler
                handler = new Handler();

                // Initialize the Runnable
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Call your function here
                        refreshStockList();

                        // Repeat the Runnable after 5 seconds
                        handler.postDelayed(this, 5000);
                    }
                };

                // Start the initial execution of the Runnable
                handler.post(runnable);
            }

            public void refreshStockList() {
                refreshedArrayStocks.clear();

                for (int i = 0; i < Stocks.size(); i++) {
                    final int index = i;
                    Stock stock = Stocks.get(i);

                    StockDataRetriever.getStockData(stock.getSymbol(), stockDataListener);
                }
            }

            @Override
            public void onAdapterUpdated() {
                // Update the adapter or perform any necessary actions
                adapter.notifyDataSetChanged();
            }

            public void fetchStockData(View view) {
                onStartUpFlag = false;
                StockDataRetriever.getStockData(String.valueOf(symbolTxtInput.getText()), stockDataListener);
                symbolTxtInput.setText("");
            }

            public void updateStockList(Stock stock){
                if(!onStartUpFlag){
                    Stocks.add(stock);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson.toJson(Stocks);
                    saveToLocalFile(json);
                }
                adapter.notifyDataSetChanged();
            }

            public void saveToLocalFile(String json){
                Context context = getApplicationContext();
                File filesDir = context.getFilesDir();
                Log.d("File content", String.valueOf(filesDir));
                String fileName = "stockData.json";
                File file = new File(filesDir, fileName);

                try{
                    FileWriter writer = new FileWriter(file);
                    writer.write(json);
                    writer.flush();
                    writer.close();
                    // File saved successfully
                } catch (IOException e) {
                    e.printStackTrace();
                    // Error occurred while saving the file
                }
            }        }

