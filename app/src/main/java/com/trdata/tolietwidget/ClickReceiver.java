package com.trdata.tolietwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by blackjack on 06.12.14.
 */
public class ClickReceiver extends BroadcastReceiver {
    final static String urlPath = "http://doors.local.trdata.com/";
    final static String settings_name = "com.trdata.toiletwidget.settings";

    void setLastClick(Context context,long last_click) {
        SharedPreferences settings = context.getSharedPreferences(settings_name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("last_widget_click", last_click);
        editor.commit();
    }

    long getLastClick(Context context) {
        SharedPreferences settings = context.getSharedPreferences(settings_name, 0);
        return settings.getLong("last_widget_click", 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ClickReceiver", "Action: " + intent.getAction());
        if (intent.getAction().equals(ToiletActions.UNLOCK_DOOR)) {
            Log.i("ClickReceiver", "Unlock door");

            long now = System.currentTimeMillis();
            Log.i("ClickReceiver","Last click: " + getLastClick(context));
            String msg = null;
            int flag = 0;
            if (now - getLastClick(context) <= 1500) {
                Toast.makeText(context, context.getString(R.string.door_open_message), Toast.LENGTH_SHORT).show();
                openTheDoor();
                setLastClick(context,0);
            } else {
                Toast.makeText(context, context.getString(R.string.door_click_again_message), Toast.LENGTH_SHORT).show();
                setLastClick(context,now);
            }

        }
    }

    void openTheDoor() {
        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httppost = new HttpPost(urlPath);
        httppost.setParams(new BasicHttpParams().setParameter("command","open_entry_door"));

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            while (inputStream.read()!=1) {}
        } catch (Exception e) {
            Log.w("ClickReceiver","Failed to open the door: " + e.getMessage());
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
    }
}
