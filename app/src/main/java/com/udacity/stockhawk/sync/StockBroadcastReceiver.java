package com.udacity.stockhawk.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.udacity.stockhawk.R;

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
            Toast.makeText(context, context.getString(R.string.toast_no_stocks_added), Toast.LENGTH_SHORT).show();
        }
    }
}
