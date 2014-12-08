package com.trdata.tolietwidget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by blackjack on 06.12.14.
 */
public class ToiletService extends IntentService {

    private static final String urlPath = "http://doors.local.trdata.com/info";

    public ToiletService() {
        super("ToiletService");
    }

    private boolean isWifiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("ToiletService", "ToiletService::onHandleIntent called.");
        if (!isWifiEnabled()) {
            publishResults(ToiletActions.TOILET_STATUS_UNKNOWN);
            Log.i("ToiletService","Wi-Fi is disabled, not checking door status");
            return;
        }

        DefaultHttpClient   httpClient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet httpget = new HttpGet(urlPath);

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
                sb.append("\n");
            }

            if (JsonParser.parseDoorJson(sb.toString()) == true ) {
                publishResults( ToiletActions.TOILET_STATUS_OPEN );
            } else {
                publishResults( ToiletActions.TOILET_STATUS_CLOSED );
            }
        } catch (Exception e) {
            Log.w("ToiletService","Failed to get wc status: " + e.getMessage());
            publishResults(ToiletActions.TOILET_STATUS_UNKNOWN);
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
    }

    private void publishResults(String status) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ToiletActions.UPDATE_TOILET_STATUS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("data", status);
        sendBroadcast(broadcastIntent);
    }


}
