package com.udacity.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.StockWidgetRemoteViewsService;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

//        CharSequence widgetText = context.getString(R.string.AAPL);
//        // Construct the RemoteViews object
          RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_item);
//        views.setTextViewText(R.id.appwidget_stock_name, widgetText);
//        views.setTextViewText(R.id.appwidget_stock_price, context.getString(R.string._55));

        //create intent to launch main activity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_stock_name, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("in onUpdate(), no, of widgets is: " + appWidgetIds.length);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Timber.d("in for loop updating widget");
            //views.setTextViewText(R.id.appwidget_stock_name, "test");
            //views.setTextViewText(R.id.appwidget_stock_price, "57");

            Intent intent = new Intent(context, StockWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_grid);

            views.setRemoteAdapter(R.id.stock_widget_grid, intent);

            //pending intent to start app on click widget click
            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            //views.setOnClickPendingIntent(R.id.stock_widget_grid, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stock_widget_grid);
            //updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void sendRefreshBroadcast(Context context){
        Timber.d("in sendRefreshBroadcast()");
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, StockWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("in onReceive()");
        //super.onReceive(context, intent);
        final String action = intent.getAction();
        if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            Timber.d("action == app widget update - update widget manager");
            //refresh all widgets
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, StockWidgetProvider.class);
            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(componentName), R.id.stock_widget_grid);
            //manager.updateAppWidget(componentName, null);
            //manager.updateAppWidget();
            super.onReceive(context, intent);
        }
    }
}

