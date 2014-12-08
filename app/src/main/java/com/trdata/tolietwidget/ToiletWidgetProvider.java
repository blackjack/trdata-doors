package com.trdata.tolietwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by blackjack on 06.12.14.
 */
public class ToiletWidgetProvider extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), ToiletService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(),0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,0,3000,pendingIntent);

        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Intent stopIntent = new Intent(context.getApplicationContext(), ToiletService.class);
        PendingIntent stopSender = PendingIntent.getService(context.getApplicationContext(),0, stopIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(stopSender);

        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ToiletWidgetProvider", "Action: " + intent.getAction());
        if (intent.getAction().equals(ToiletActions.UPDATE_TOILET_STATUS)) {
            String status = intent.getExtras().getString("data");
            Log.i("WC Status","Got WC door status: " + status);
            setWCStatus(context,status);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, ClickReceiver.class);
        intent.setAction(ToiletActions.UNLOCK_DOOR);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.toilet_layout);
        views.setOnClickPendingIntent(R.id.imageView, pendingIntent);

        updateWidget(context,views);
    }

    private void setWCStatus(Context context, String status) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.toilet_layout);

        String status_str = context.getString(R.string.door_status);
        String status_detail = null;
        if (status.equals(ToiletActions.TOILET_STATUS_OPEN)) {
            updateViews.setImageViewResource(R.id.imageView,R.drawable.open);
            status_detail = context.getString(R.string.door_open);
        } else if (status.equals(ToiletActions.TOILET_STATUS_CLOSED)) {
            updateViews.setImageViewResource(R.id.imageView,R.drawable.close);
            status_detail = context.getString(R.string.door_closed);
        } else {
            updateViews.setImageViewResource(R.id.imageView,R.drawable.unknown);
            status_detail = context.getString(R.string.door_unknown);
        }
        updateViews.setTextViewText(R.id.textView,String.format(status_str,status_detail));
        updateWidget(context,updateViews);
    }

    private void updateWidget(Context context, RemoteViews views) {
        ComponentName myWidget = new ComponentName(context,ToiletWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, views);
    }
}
