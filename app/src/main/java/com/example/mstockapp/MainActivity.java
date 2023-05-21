package com.example.mstockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements StockListAdapter.AdapterCallback {

    private static final String TAG = "MainActivity";
    EditText symbolTxtInput;
    ArrayList<Stock> Stocks = new ArrayList<Stock>();
    ListView stockList;
    StockListAdapter adapter;
    String symbol;
    String price;
    String dailyChange;
    String jsonContent;
    int onStartUpFlag;


    private StockDataRetriever.StockDataListener stockDataListener = new StockDataRetriever.StockDataListener() {
        @Override
        public void onStockDataReceived(String symbol, double latestPrice, double dailyChange) {
            // Handle the received stock data
            Log.e(TAG, "Symbol: " + symbol + ", Latest Price: " + latestPrice + ", Daily Change: " + dailyChange);
            Stock stock = new Stock(symbol,String.valueOf(latestPrice),String.valueOf(dailyChange));
            runOnUiThread(() -> updateStockList(stock));

        }

        @Override
        public void onStockDataError(String error) {
            // Handle the error in retrieving stock data
            Log.e(TAG, "Error: " + error);
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

        onStartUpFlag = 1;
        for (Stock stock : stocks) {
            // Process each stock object
            StockDataRetriever.getStockData(stock.getSymbol(), stockDataListener);
            Stocks.add(stock);
            adapter.notifyDataSetChanged();
        }



        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, LineChartGraph.class);
                intent.putExtra("key", Stocks.get(i).symbol);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onAdapterUpdated() {
        // Update the adapter or perform any necessary actions
        adapter.notifyDataSetChanged();
    }

    public void fetchStockData(View view) {
        onStartUpFlag = 0;
        StockDataRetriever.getStockData(String.valueOf(symbolTxtInput.getText()), stockDataListener);
    }

    public void updateStockList(Stock stock){
        if(onStartUpFlag == 0){
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
    }
}