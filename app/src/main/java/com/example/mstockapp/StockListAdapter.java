package com.example.mstockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StockListAdapter extends ArrayAdapter<Stock> {
    private Context mContext;
    int mResource;
    public StockListAdapter(Context context, int resource, ArrayList<Stock> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String symbol = getItem(position).getSymbol();
        String price = getItem(position).getPrice();
        String dailyChange = getItem(position).getDailyChange();

        Stock stock = new Stock(symbol, price, dailyChange);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvSymbol = (TextView) convertView.findViewById(R.id.textView);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.textView2);
        TextView tvDailyChange = (TextView) convertView.findViewById(R.id.textView3);

        tvSymbol.setText(symbol);
        tvPrice.setText(price);
        tvDailyChange.setText(dailyChange);
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        // Add custom logic here before notifying the data set changed

        // Call the superclass implementation to notify the data set changed
        super.notifyDataSetChanged();
    }
}
