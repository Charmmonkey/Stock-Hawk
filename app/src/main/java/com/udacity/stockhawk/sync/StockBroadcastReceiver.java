package com.udacity.stockhawk.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by jerye on 1/17/2017.
 */

public class StockBroadcastReceiver extends BroadcastReceiver {
    String mSymbol;
    public StockBroadcastReceiver(String symbol) {
        mSymbol = symbol;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(QuoteSyncJob.RECEIVE_NONSTOCK)) {
            Toast.makeText(context, "Stock does not exist", Toast.LENGTH_SHORT).show();
        }
        if (intent.getAction().equals(QuoteSyncJob.ACTION_DATA_UPDATED)) {
            Toast.makeText(context, "Added To Watchlist" , Toast.LENGTH_SHORT).show();
        }
    }
}
