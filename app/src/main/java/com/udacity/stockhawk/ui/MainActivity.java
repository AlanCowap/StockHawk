package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;
    private Float[] floatArray;

    @Override
    public void onClick(String symbol) {
        //TODO add correct flaot array here - switch?
        Timber.d("Symbol clicked: %s", symbol);
        Intent intent = new Intent(this, StockHistoryActivity.class);
        intent.putExtra("stockSymbol", symbol);
        intent.putExtra("floatArray", floatArray);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("in onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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


        Cursor cursor = getAllhistory();
        displayCursor(cursor);

    }

    private void displayCursor(Cursor cursor) {
        Timber.d("cursor count is: " + cursor.getCount());

        while (cursor.moveToNext()){
            Timber.d("cursor column count is: " + cursor.getColumnCount());
            Timber.d("column name is: " + cursor.getColumnName(0));
            Timber.d("value in column is: " + cursor.getString(0));

            Timber.d("column name is: " + cursor.getColumnName(1));
            Timber.d("value in column is: " + cursor.getString(1));

            Timber.d("column name is: " + cursor.getColumnName(2));
            Timber.d("value in column is: " + cursor.getString(2));

            Timber.d("column name is: " + cursor.getColumnName(3));
            Timber.d("value in column is: " + cursor.getString(3));

            Timber.d("column name is: " + cursor.getColumnName(4));
            Timber.d("value in column is: " + cursor.getString(4));

            Timber.d("column name is: " + cursor.getColumnName(5));
            Timber.d("value in column is: " + cursor.getString(5));

            initAllStockHistory(cursor.getString(5));
        }
    }

    private void initAllStockHistory(String history) {
        Timber.d("in initAllStockHistory()");
        ArrayList<Float[]> stockHistory = new ArrayList<>();
        stockHistory.add(convertStringHistoryToFloatArray(history));
    }

    private Float[] convertStringHistoryToFloatArray(String history) {
        Timber.d("in convertStringHistoryToFloatArray()");
        ArrayList<String> splitString = new ArrayList<>();
        String[] strArray = (history.split("\n"));

        ArrayList<String[]> stArrList = new ArrayList<>();

        for(String str: strArray){
            stArrList.add(str.split(","));
        }
        Timber.d("size of split array is: " + stArrList.size());



        Float[] floatArray = new Float[stArrList.size()];
        for(int i = 0; i < stArrList.size(); ++i){
            //if(i % 2 == 0) continue;
            Timber.d("float price is: " + stArrList.get(i)[1]);
            floatArray[i] = Float.parseFloat(stArrList.get(i)[1].trim());
            //Timber.d(strArray[i]);
        }
        return floatArray;
    }

    private Cursor getAllhistory() {
        try{
            return getContentResolver().query(Contract.Quote.URI,
                    null,
                    null,
                    null,
                    null);
        }catch (Exception e){
            Timber.d("failed to get all history from DB");
            e.printStackTrace();
            return null;
        }
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

    void addStock(String symbol, boolean isValid) {
        Timber.d("in addStock(), symbol received is: " + symbol.toString());
        if (isValid) {

            if (networkUp()) {
                Timber.d("in addStock(), network is up, will refresh layout");
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            Timber.d("calling PrefUtils.addStock passing args: " + this + " and: " + symbol);
            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }else {
            //TODO working on showing error - getting looper error message, cant create handler inside thread....
            Toast.makeText(getApplicationContext(), "The stock symbol you entered does not exist, please try another", Toast.LENGTH_SHORT).show();
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
}