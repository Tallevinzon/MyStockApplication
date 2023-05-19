package com.example.mstockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    EditText symbolTxtInput;
    ArrayList<Stock> Stocks = new ArrayList<Stock>();
    ListView stockList;
    StockListAdapter adapter;
    String symbol;
    String price;
    String dailyChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Started.");
        symbolTxtInput = (EditText) findViewById(R.id.symbolTxtInput);
        stockList = (ListView) findViewById(R.id.StockList);
        adapter = new StockListAdapter(this, R.layout.activity_list_view, Stocks);
        stockList.setAdapter(adapter);

        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("List_View", "Item is clicked @ position " + i);
            }
        });
    }


    public void fetchStockData(View view) {
        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = "JSIFUJ8C1BIJZAH4";
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbolTxtInput.getText() + "&apikey=" + apiKey;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseData = responseBody.string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String globalQuote = jsonResponse.getString("Global Quote");
                            String[] pairs = globalQuote.split(",");// Create a map to store the key-value pairs
                            Map<String, String> dataMap = new HashMap<>();

                            if(!pairs[0].equals("{}")) {
                                for (String pair : pairs) {
                                    String[] keyValue = pair.split(":");
                                    String key = keyValue[0].replaceAll("\"", "").trim();
                                    String value = keyValue[1].replaceAll("\"", "").trim();
                                    dataMap.put(key, value);
                                }// Access the extracted key-value pairs
                            }
                            symbol = dataMap.get("{01. symbol");
                            String open = dataMap.get("02. open");
                            String high = dataMap.get("03. high");
                            String low = dataMap.get("04. low");
                            price = dataMap.get("05. price");
                            String volume = dataMap.get("06. volume");
                            String latestTradingDay = dataMap.get("07. latest trading day");
                            String previousClose = dataMap.get("08. previous close");
                            dailyChange = dataMap.get("09. change");
                            String changePercent = dataMap.get("10. change percent");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("StockData", responseData);

                        // Process the response data as per your requirements
                        // Parse JSON data or perform other operations
                    }
                } else {
                    // Handle error response
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();  // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(symbol != null){
            Stocks.add(new Stock(symbol, price, dailyChange));// Print the response to the console
            adapter.notifyDataSetChanged();
        }
    }
}