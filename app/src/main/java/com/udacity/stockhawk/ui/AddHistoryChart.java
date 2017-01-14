package com.udacity.stockhawk.ui;

import android.content.Context;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by jerye on 1/13/2017.
 */

public class AddHistoryChart {
    View mView;
    public final String mHistoryData;
    List<Entry> entries = new ArrayList<Entry>();

    public AddHistoryChart(View view, String historyData) {
        mView = view;
        mHistoryData = historyData;
    }

    public void createChart() {
        LineChart chart = (LineChart) mView.findViewById(R.id.line_chart);
        List<String> list = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().split(mHistoryData));
        Collections.reverse(list);
        Timber.d(list.toString());
        Timber.d(Integer.toString(list.size()));
        Timber.d(list.get(0));
        int i = 1;
        for (String intervalData : list) {
            String[] xyData = splitData(intervalData);
            Float xData = (float)i;
            Float yData = Float.parseFloat(xyData[1]);

            entries.add(new Entry(xData, yData));
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        Timber.d(dataSet.toString());
        LineData lineData = new LineData(dataSet);
        Timber.d(lineData.toString());
        chart.setData(lineData);
        chart.invalidate();

    }

    private String[] splitData(String singleData) {
        return singleData.split(",", 2);
    }


}
