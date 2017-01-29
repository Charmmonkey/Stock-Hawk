package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by jerye on 1/22/2017.
 */

public class StockWidgetRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        Context mContext;
        Intent mIntent;
        Cursor mCursor = null;
        String symbol;
        String price;
        String absolute_change;
        String percentage_change;
        String[] projection = {
                Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
                Contract.Quote.COLUMN_SYMBOL,
                Contract.Quote.COLUMN_PRICE,
                Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                Contract.Quote.COLUMN_PERCENTAGE_CHANGE};
        int INDEX_COLUMN_ID = 0;
        int INDEX_COLUMN_SYMBOL = 1;
        int INDEX_COLUMN_PRICE = 2;
        int INDEX_COLUMN_ABSOLUTE_CHANGE = 3;
        int INDEX_COLUMN_PERCENTAGE_CHANGE = 4;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat dollarFormat;
        private final DecimalFormat percentageFormat;


        StockRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mIntent = intent;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {

        }

        @Override
        public int getCount() {
            return mContext == null ? 0 : mCursor.getCount();
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                mCursor.close();
            }
            long identityToken = Binder.clearCallingIdentity();
            mCursor = getContentResolver().query(Contract.Quote.URI, projection, null, null, null);
            Binder.restoreCallingIdentity(identityToken);

        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public RemoteViews getViewAt(int position) {

            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }

            int widgetWidth = StockWidgetProvider.widgetWidth;
            int widgetWidthLarge = mContext.getResources().getDimensionPixelSize(R.dimen.widget_width_large);
            int widgetWidthDefault = mContext.getResources().getDimensionPixelSize(R.dimen.widget_width_default);
            int widgetWidthSmall = mContext.getResources().getDimensionPixelSize(R.dimen.widget_width_small);

            RemoteViews widgetListItemView;

            if (widgetWidth >= widgetWidthLarge) {
                widgetListItemView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_large);
            } else if (widgetWidth >= widgetWidthDefault){
                widgetListItemView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
            } else {
                widgetListItemView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_small);
            }

            symbol = mCursor.getString(INDEX_COLUMN_SYMBOL);
            price = dollarFormat.format(mCursor.getFloat(INDEX_COLUMN_PRICE));
            absolute_change = dollarFormatWithPlus.format(mCursor.getFloat(INDEX_COLUMN_ABSOLUTE_CHANGE));
            percentage_change = dollarFormatWithPlus.format(mCursor.getFloat(INDEX_COLUMN_PERCENTAGE_CHANGE));

            final Intent fillIntent = new Intent().putExtra(getString(R.string.widget_click_position), position);

//            RemoteViews widgetListItemView = new RemoteViews(getPackageName(), R.layout.widget_list_item);
            widgetListItemView.setTextViewText(R.id.widget_symbol, symbol);
            widgetListItemView.setTextViewText(R.id.widget_price, price);
            widgetListItemView.setTextViewText(R.id.widget_percentage_change, "(" + percentage_change +"%)");
            widgetListItemView.setTextViewText(R.id.widget_absolute_change, absolute_change);
            widgetListItemView.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);

            return widgetListItemView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position))
                return mCursor.getLong(INDEX_COLUMN_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
