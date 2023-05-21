package com.example.mstockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart_graph);

        chart = findViewById(R.id.line_chart);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        // Retrieve the key from the intent
        String receivedKey = getIntent().getStringExtra("key");

        Log.d("Received Key", receivedKey);
        // Make API request and plot the graph
        fetchStockData(receivedKey);

        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchStockData(String symbol) {
        String apiKey = "JSIFUJ8C1BIJZAH4";
        String function = "TIME_SERIES_DAILY";
        String url = "https://www.alphavantage.co/query?function=" + function + "&symbol=" + symbol + "&apikey=" + apiKey;

        // Make an API request to Alpha Vantage
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("APIRequest", "Failed to fetch stock data: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    List<Entry> entries = parseStockData(responseData);
                    runOnUiThread(() -> plotStockGraph(entries));
                    Log.e("APIResponse", "API request success");
                } else {
                    Log.e("APIResponse", "API request failed with response code: " + response.code());
                }
            }
        });
    }

    private List<Entry> parseStockData(String responseData) {
        List<Entry> entries = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");

            Iterator<String> keys = timeSeries.keys();
            while (keys.hasNext()) {
                String date = keys.next();
                JSONObject data = timeSeries.getJSONObject(date);
                float closingPrice = data.getString("4. close") != null ? Float.parseFloat(data.getString("4. close")) : 0;

                entries.add(new Entry(Float.parseFloat(date), closingPrice));
            }

            Collections.sort(entries, new EntryXComparator());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return entries;
    }

    private void plotStockGraph(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Stock Data");
        dataSet.setDrawValues(false);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1f);

        Description description = new Description();
        description.setText("Stock Prices");
        chart.setDescription(description);

        chart.setData(lineData);
        chart.invalidate();
    }
}