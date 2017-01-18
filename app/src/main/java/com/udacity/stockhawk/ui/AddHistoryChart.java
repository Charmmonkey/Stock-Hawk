package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawLineChartTouchListener;
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

    public AddHistoryChart(View parentViewOfHistoryChart, String historyData) {
        mView = parentViewOfHistoryChart;
        mHistoryData = historyData;
    }

    public void createChart() {
        LineChart chart = (LineChart) mView.findViewById(R.id.line_chart);
        final TextView textView = (TextView) mView.findViewById(R.id.highlighted_value);
        List<String> list = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().split(mHistoryData));
        Collections.reverse(list);

        int i = 1;
        for (String intervalData : list) {
            String[] xyData = splitData(intervalData);
            Float xData = (float) i;
            Float yData = Float.parseFloat(xyData[1]);

            entries.add(new Entry(xData, yData));
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");

        dataSet.setCircleRadius((float) 1.5);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(3);
        dataSet.setHighlightLineWidth(3);
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        LineData lineData = new LineData(dataSet);


        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setDrawMarkers(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getLineData().setDrawValues(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                textView.setText(Float.toString(entry.getY()));
            }

            @Override
            public void onNothingSelected() {
            }
        });



        chart.invalidate();

    }

    private class CustomOnTouchListener extends OnDrawLineChartTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return super.onTouch(v, event);
        }
    }

    private String[] splitData(String singleData) {
        return singleData.split(",", 2);
    }


}
