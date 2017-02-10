package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    public static final String RECEIVE_NONSTOCK = "com.udacity.stockhawk.RECEIVE_NONSTOCK";

    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int DAYS_7_HISTORY = 7;
    private static final int MONTHS_1_HISTORY = 1;
    private static final int MONTHS_6_HISTORY = 6;
    private static final int YEARS_1_HISTORY = 1;
    private static final int YEARS_2_HISTORY = 2;
    private static boolean isStockExist = true;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from1week = Calendar.getInstance();
        Calendar from1month = Calendar.getInstance();
        Calendar from6month = Calendar.getInstance();
        Calendar from1year = Calendar.getInstance();
        Calendar from2year = Calendar.getInstance();

        from1week.add(Calendar.DATE, -DAYS_7_HISTORY);
        from1month.add(Calendar.MONTH, -MONTHS_1_HISTORY);
        from6month.add(Calendar.MONTH, -MONTHS_6_HISTORY);
        from1year.add(Calendar.YEAR, -YEARS_1_HISTORY);
        from2year.add(Calendar.YEAR, -YEARS_2_HISTORY);


        Calendar to = Calendar.getInstance();

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);

            Iterator<String> iterator = stockCopy.iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                if (quote.getPrice() == null){
                    Intent intentForNonexistentStock = new Intent(RECEIVE_NONSTOCK);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentForNonexistentStock);
                    isStockExist = false;
                    break;
                }

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();


                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history1week = stock.getHistory(from1week, to, Interval.DAILY);
                List<HistoricalQuote> history1month = stock.getHistory(from1month, to, Interval.DAILY);
                List<HistoricalQuote> history6month = stock.getHistory(from6month, to, Interval.DAILY);
                List<HistoricalQuote> history1year = stock.getHistory(from1year, to, Interval.WEEKLY);
                List<HistoricalQuote> history2year = stock.getHistory(from2year, to, Interval.WEEKLY);

                List<List<HistoricalQuote>> historyList = new ArrayList<List<HistoricalQuote>>();
                historyList.add(history1week);
                historyList.add(history1month);
                historyList.add(history6month);
                historyList.add(history1year);
                historyList.add(history2year);


                // for each list of historical quotes (1w, 1m, 6m, 1y, 2y)
                for (List<HistoricalQuote> eachHistoryList : historyList){

                    StringBuilder historyBuilder = new StringBuilder();

                    for (HistoricalQuote it : eachHistoryList) { // for each interval
                        historyBuilder.append(it.getDate().getTimeInMillis()); // get time
                        historyBuilder.append(", ");
                        historyBuilder.append(it.getClose()); // get closing market value
                        historyBuilder.append("\n");
                    }

                    quoteCV.put(Contract.Quote.HISTORY_COLUMNS[historyList.indexOf(eachHistoryList)], historyBuilder.toString());
                }

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            if(isStockExist){
                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                context.sendBroadcast(dataUpdatedIntent);
            }

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
