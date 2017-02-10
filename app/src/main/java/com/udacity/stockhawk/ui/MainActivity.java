package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.sync.StockBroadcastReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static String newestAddedStock;
    private static AddHistoryChart mHistoryChart;
    private int bnvItemNumber = 2;
    private static View mParentView;
    private static Cursor mCursorAtClickedPosition;
    private static int widgetClickedPosition;
    private static final int STOCK_LOADER = 0;
    private StockAdapter adapter;
    private StockBroadcastReceiver stockBroadcastReceiver = new StockBroadcastReceiver(newestAddedStock);
    LocalBroadcastManager bManager;
    BottomNavigationViewEx bottomNavigationView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;


    @Override
    public void onClick(int adapterPosition) {
        widgetClickedPosition = adapterPosition;
        mCursorAtClickedPosition.moveToPosition(widgetClickedPosition);
        // Add the history chart of the clicked stock
        bottomNavigationView.setCurrentItem(bnvItemNumber);
        mHistoryChart = new AddHistoryChart(this, mParentView, mCursorAtClickedPosition, Contract.Quote.HISTORY_POSITIONS_ARRAY[bnvItemNumber]);
        mHistoryChart.createChart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("onCreate"
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);


        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QuoteSyncJob.RECEIVE_NONSTOCK);
        bManager.registerReceiver(stockBroadcastReceiver, intentFilter);


        Bundle bundleExtraFromWidget = this.getIntent().getExtras();
        try {
            widgetClickedPosition = bundleExtraFromWidget.getInt(getString(R.string.widget_click_position));
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mParentView = findViewById(R.id.history_layout);

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);

        bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
        bottomNavigationView.enableItemShiftingMode(false);
        bottomNavigationView.setIconVisibility(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mCursorAtClickedPosition.moveToPosition(widgetClickedPosition);
                switch (item.getItemId()) {
                    case R.id.history_1w:
                        mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_1_WEEK);
                        mHistoryChart.createChart();
                        bnvItemNumber = 0;
                        break;
                    case R.id.history_1m:
                        mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_1_MONTH);
                        mHistoryChart.createChart();
                        Timber.e("cursor position: " + mCursorAtClickedPosition.getPosition());
                        bnvItemNumber = 1;

                        break;
                    case R.id.history_6m:
                        mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_6_MONTH);
                        mHistoryChart.createChart();
                        bnvItemNumber = 2;

                        break;
                    case R.id.history_1y:
                        mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_1_YEAR);
                        mHistoryChart.createChart();
                        bnvItemNumber = 3;

                        break;
                    case R.id.history_2y:
                        mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_2_YEAR);
                        mHistoryChart.createChart();
                        bnvItemNumber = 4;

                        break;
                }
                return true;
            }
        });
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            newestAddedStock = symbol;
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
        if (data.moveToPosition(widgetClickedPosition)) {
            mCursorAtClickedPosition = data;
            mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_6_MONTH);
            mHistoryChart.createChart();
            bottomNavigationView.setCurrentItem(bnvItemNumber);
            Timber.e("widgetClickedPosition at loadFinish " + widgetClickedPosition);
            Timber.e("mCursor get position at loadFinish " + mCursorAtClickedPosition.getPosition());


        } else if (data.moveToFirst()) {
            mCursorAtClickedPosition = data;
            mHistoryChart = new AddHistoryChart(getApplicationContext(), mParentView, mCursorAtClickedPosition, Contract.Quote.POSITION_HISTORY_6_MONTH);
            mHistoryChart.createChart();
            Timber.e("first called");
        }


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {

        bManager.unregisterReceiver(stockBroadcastReceiver);
        super.onDestroy();

        widgetClickedPosition = 0;
    }
}
