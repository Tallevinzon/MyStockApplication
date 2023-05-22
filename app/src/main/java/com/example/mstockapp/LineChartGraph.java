package com.example.mstockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LineChartGraph extends AppCompatActivity {

    private LineChart chart;
    String receivedKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart_graph);

        chart = findViewById(R.id.line_chart);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        // Retrieve the key from the intent
        receivedKey = getIntent().getStringExtra("key");

        Log.d("Received Key", receivedKey);
        // Make API request and plot the graph
        fetchStockData(receivedKey,"1m");

        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button oneDayButton = (Button) findViewById(R.id.oneDayBtn);
        oneDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"1d");
            }
        });

        Button fiveDaysButton = (Button) findViewById(R.id.fiveDaysBtn);
        fiveDaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"5d");
            }
        });
        Button oneMonthButton = (Button) findViewById(R.id.oneMonthBtn);
        oneMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"1m");
            }
        });

        Button sixMonthButton = (Button) findViewById(R.id.sixMonthBtn);
        sixMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"6m");
            }
        });

        Button ytdButton = (Button) findViewById(R.id.YTDBtn);
        ytdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"ytd");
            }
        });


        Button oneYearButton = (Button) findViewById(R.id.oneYearBtn);
        oneYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"1y");
            }
        });

        Button fiveYearsButton = (Button) findViewById(R.id.fiveYearsBtn);
        fiveYearsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"5y");
            }
        });

        Button maxButton = (Button) findViewById(R.id.maxBtn);
        maxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchStockData(receivedKey,"max");
            }
        });

    }

    private void fetchStockData(String symbol,String periodTime) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://cloud.iexapis.com/stable/stock/{symbol}/chart/"+ periodTime + "?token=sk_ae7d7b54f1eb44268f556d14736d265f";

        Request request = new Request.Builder()
                .url(url.replace("{symbol}", receivedKey))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // Parse the response data and extract the required values

                    // Update the UI on the main thread
                    runOnUiThread(() -> {
                        // Plot the stock data on the line chart
                        List<Entry> entries = parseStockData(responseData);
                        plotStockGraph(entries, chart);
                    });
                }
            }
        });
    }

    private List<Entry> parseStockData(String responseData) {
        List<Entry> entries = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(responseData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEntry = jsonArray.getJSONObject(i);
                String date = jsonEntry.getString("date");
                double closePrice = jsonEntry.getDouble("close");

                entries.add(new Entry(i, (float) closePrice));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private void plotStockGraph(List<Entry> entries, LineChart lineChart) {
        LineDataSet dataSet = new LineDataSet(entries, "Stock Data");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize the appearance of the line chart
        // You can set various properties like colors, labels, axis, etc.
        // For example:
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawValues(false);

        lineChart.invalidate(); // Refresh the chart
    }
}