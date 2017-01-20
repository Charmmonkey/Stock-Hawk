package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by jerye on 1/13/2017.
 */

public class AddHistoryChart {
    View mView;
    public final String mHistoryData;
    List<Entry> entries = new ArrayList<Entry>();
    List<String> xData = new ArrayList<>();
    @BindView(R.id.line_chart)
    LineChart chart;
    @BindView(R.id.highlighted_value_price)
    TextView textViewOfPrice;
    @BindView(R.id.highlighted_value_date)
    TextView textViewOfDate;

    public AddHistoryChart(View parentViewOfHistoryChart, String historyData) {
        mView = parentViewOfHistoryChart;
        mHistoryData = historyData;
    }

    public void createChart() {
        ButterKnife.bind(this, mView);

        List<String> list = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().split(mHistoryData));
        Collections.reverse(list);

        int i = 0;
        for (String intervalData : list) {
            String[] xyData = splitData(intervalData);
            Float xDataCount = (float) i;
            xData.add(xyData[0]);
            Float yData = Float.parseFloat(xyData[1]);

            entries.add(new Entry(xDataCount, yData));
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
        chart.setClipToPadding(false);
        chart.setViewPortOffsets(0,0,0,0);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getLineData().setDrawValues(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                textViewOfPrice.setText(Float.toString(entry.getY()));

                int indexOfX = (int) entry.getX();
                long dateValueAtX = Long.valueOf(xData.get(indexOfX));
                String dateAtIndexX = convertEpochTimeToMMMDDYYYY(dateValueAtX);
                textViewOfDate.setText(dateAtIndexX);
            }

            @Override
            public void onNothingSelected() {
            }
        });


        chart.invalidate();

    }

    private class CustomOnTouchListener extends OnDrawLineChartTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return super.onTouch(v, event);
        }
    }

    private String[] splitData(String singleData) {
        return singleData.split(",", 2);
    }


    private String convertEpochTimeToMMMDDYYYY(long epochTime){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
        return sdf.format(new Date(epochTime));
    }
}
