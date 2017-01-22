package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawLineChartTouchListener;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by jerye on 1/13/2017.
 */

public class AddHistoryChart {
    private View mView;
    private Cursor mCursor;
    private String mStockSymbol;
    private String mCurrentPrice;
    private String mHistoryData;
    public static LineDataSet dataSet;
    private List<Entry> entries = new ArrayList<Entry>();
    private List<String> xData = new ArrayList<>();
    private final DecimalFormat dollarFormat;

    @BindView(R.id.line_chart)
    LineChart chart;
    @BindView(R.id.highlighted_value_price)
    TextView textViewOfPrice;
    @BindView(R.id.highlighted_value_date)
    TextView textViewOfDate;

    public AddHistoryChart(View parentViewOfHistoryChart, Cursor cursorAtClickedPosition, int positionHistoryChart) {
        mView = parentViewOfHistoryChart;
        mCursor = cursorAtClickedPosition;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

        mHistoryData = mCursor.getString(positionHistoryChart);
        mStockSymbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
        mCurrentPrice = dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE));


    }

    public void createChart() {
        ButterKnife.bind(this, mView);

        setCurrentPriceViews();

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

        dataSet = new LineDataSet(entries, "Label");

        dataSet.setCircleRadius((float) 1.5);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(3);
        dataSet.setHighlightLineWidth(3);
        dataSet.setHighLightColor(Color.BLACK);
        dataSet.setHighLightColor(Color.TRANSPARENT);
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
        chart.setViewPortOffsets(0, 0, 0, 0);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getLineData().setDrawValues(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                textViewOfPrice.setText(dollarFormat.format(entry.getY()));

                int indexOfX = (int) entry.getX();
                long dateValueAtX = Long.valueOf(xData.get(indexOfX));
                String dateAtIndexX = convertEpochTimeToMMMDDYYYY(dateValueAtX);
                textViewOfDate.setText(dateAtIndexX);
            }

            @Override
            public void onNothingSelected() {
                setCurrentPriceViews();
            }
        });
        chart.setOnChartGestureListener(new CustomOnChartGestureListener());


        chart.invalidate();

    }



    private void setCurrentPriceViews() {
        textViewOfPrice.setText(mCurrentPrice);
        textViewOfDate.setText("Today");
    }

    private String[] splitData(String singleData) {
        return singleData.split(",", 2);
    }

    private String convertEpochTimeToMMMDDYYYY(long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
        return sdf.format(new Date(epochTime));
    }

    private class CustomOnChartGestureListener implements OnChartGestureListener {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            dataSet.setHighLightColor(Color.WHITE);}
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            dataSet.setHighLightColor(Color.TRANSPARENT);
            setCurrentPriceViews();}
        @Override
        public void onChartLongPressed(MotionEvent me) {}
        @Override
        public void onChartDoubleTapped(MotionEvent me) {}
        @Override
        public void onChartSingleTapped(MotionEvent me) {}
        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {}
    }
}
