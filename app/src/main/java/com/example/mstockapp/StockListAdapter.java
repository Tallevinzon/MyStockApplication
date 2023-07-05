package com.example.mstockapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StockListAdapter extends ArrayAdapter<Stock> {
    private Context mContext;
    int mResource;
    List<Stock> stockList;

    private AdapterCallback callback;

    // Define the interface
    public interface AdapterCallback {
        void onAdapterUpdated();
    }

    // Set the callback
    public void setCallback(AdapterCallback callback) {
        this.callback = callback;
    }
    public StockListAdapter(Context context, int resource, ArrayList<Stock> Stocks) {
        super(context, resource, Stocks);
        mContext = context;
        mResource = resource;
        stockList = Stocks;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String symbol = getItem(position).getSymbol();
        String price = getItem(position).getPrice();
        String dailyChange = getItem(position).getDailyChange();

        Stock stock = new Stock.StockBuilder()
                .setSymbol(symbol)
                .setPrice(price)
                .setDailyChange(dailyChange)
                .build();


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvSymbol = (TextView) convertView.findViewById(R.id.textView);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.textView2);
        TextView tvDailyChange = (TextView) convertView.findViewById(R.id.textView3);

        Stock positionToRemove = stockList.get(position);

        Button button = convertView.findViewById(R.id.removeBtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle button click for this item
                removeItem(positionToRemove); // Call a method to remove the item from the list
            }
        });

        // Set the click listener for the parent ListView item
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ArrayAdapter","print works");
                // Start a new activity from the MainActivity
                Context context = parent.getContext();
                Intent intent = new Intent(context, LineChartGraph.class);
                Log.e("Position:", stockList.get(position).getSymbol());
                intent.putExtra("key", stockList.get(position).getSymbol());
                context.startActivity(intent);
            }
        });

        tvSymbol.setText(symbol);
        tvPrice.setText(price);
        tvDailyChange.setText(dailyChange);
        return convertView;
    }

    private void removeItem(Stock stock) {
        // Remove the stock item from the list
        stockList.remove(stock);
        if (callback != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(stockList);
            saveToLocalFile(json);
            callback.onAdapterUpdated();
        }
//        notifyDataSetChanged(); // Notify the adapter that the data has changed
        Log.e("Yep: ","Item has been removed");
    }

    public void saveToLocalFile(String json){
        Context context = mContext.getApplicationContext();
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

    @Override
    public void notifyDataSetChanged() {
        // Add custom logic here before notifying the data set changed

        // Call the superclass implementation to notify the data set changed
        super.notifyDataSetChanged();
    }
}
