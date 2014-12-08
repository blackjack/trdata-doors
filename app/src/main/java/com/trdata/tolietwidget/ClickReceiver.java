package com.trdata.tolietwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

        class OpenTheDoorTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost(urlPath);

                InputStream inputStream = null;
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("command", "open_entry_door"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    httpClient.execute(httppost);

                } catch (Exception e) {
                    Log.w("ClickReceiver","Failed to open the door: " + e.getMessage());
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return null;
            }
        }

        new OpenTheDoorTask().execute();
    }
}
