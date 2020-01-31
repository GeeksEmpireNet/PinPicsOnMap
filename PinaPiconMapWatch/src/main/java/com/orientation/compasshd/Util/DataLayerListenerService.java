/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.orientation.compasshd.Util.Functions.FunctionsClass;
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataLayerListenerService extends WearableListenerService {

    FunctionsClass functionsClass;
    public static String nodeId = null;

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        functionsClass = new FunctionsClass(getApplicationContext());

        for (DataEvent dataEvent : dataEventBuffer) {
            Uri uri = dataEvent.getDataItem().getUri();
            nodeId = uri.getHost();

            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                switch (dataEvent.getDataItem().getUri().getPath()) {
                    case "/locations": {
                        FunctionsClassDebug.Companion.PrintDebug("*** /locations --- latlong ***");

                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                        Asset profileAsset = dataMapItem.getDataMap().getAsset("latlong");
                        extractAssetDataLocations(profileAsset);

                        break;
                    }
                    case "/details": {
                        FunctionsClassDebug.Companion.PrintDebug("*** /details --- information ***");

                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                        Asset profileAsset = dataMapItem.getDataMap().getAsset("information");
                        extractAssetDataPreferences(profileAsset);

                        break;
                    }
                }
            }
        }
    }

    public void extractAssetDataLocations(Asset asset) {
        if (asset == null) {
            return;
        }

        try {
            InputStream assetInputStream =
                    Tasks.await(Wearable.getDataClient(getApplicationContext()).getFdForAsset(asset))
                            .getInputStream();

            if (assetInputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetInputStream));
                String receiveString = "";
                deleteFile(".Locations");
                while ((receiveString = bufferedReader.readLine()) != null) {
                    functionsClass.saveFileAppendLine(".Locations", receiveString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extractAssetDataPreferences(Asset asset) {
        if (asset == null) {
            return;
        }

        try {
            InputStream assetInputStream =
                    Tasks.await(Wearable.getDataClient(getApplicationContext()).getFdForAsset(asset))
                            .getInputStream();

            if (assetInputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetInputStream));
                String receiveString = "";
                deleteFile(".Details");
                while ((receiveString = bufferedReader.readLine()) != null) {
                    functionsClass.saveFileAppendLine(".Details", receiveString);
                }
                if (getFileStreamPath(".Details").exists()) {
                    sendBroadcast(new Intent("NEW_LOCATION_DATA"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
