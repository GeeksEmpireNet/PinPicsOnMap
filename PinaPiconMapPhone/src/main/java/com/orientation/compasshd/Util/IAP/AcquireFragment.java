/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.IAP;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orientation.compasshd.Maps.TimeCheckMap;
import com.orientation.compasshd.R;
import com.orientation.compasshd.Util.Functions.FunctionsClass;
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug;
import com.orientation.compasshd.Util.IAP.billing.BillingProvider;
import com.orientation.compasshd.Util.IAP.skulist.SkusAdapter;
import com.orientation.compasshd.Util.IAP.skulist.row.SkuRowData;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class AcquireFragment extends DialogFragment implements View.OnClickListener {

    Activity activity;
    Context context;

    FunctionsClass functionsClass;

    RecyclerView recyclerView;
    ProgressBar progressBar;

    HorizontalScrollView demoHorizontalScrollView;
    LinearLayout demoList;
    TextView demoDescription;

    SkusAdapter skusAdapter;

    MaterialButton materialButtonShare;

    BillingProvider billingProvider;

    TreeMap<Integer, Drawable> mapIndexDrawable = new TreeMap<Integer, Drawable>();
    TreeMap<Integer, Uri> mapIndexURI = new TreeMap<Integer, Uri>();

    RequestManager requestManager;

    int screenshotsNumber = 6, glideLoadCounter = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = getActivity();
        this.context = getContext();

        functionsClass = new FunctionsClass(activity);
        requestManager = Glide.with(context);

        if (TimeCheckMap.Companion.getTime().equals("day")) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AppThemeLight);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AppThemeDark);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.iap_fragment, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        progressBar = (ProgressBar) root.findViewById(R.id.progress_circular);
        demoHorizontalScrollView = (HorizontalScrollView) root.findViewById(R.id.demoHorizontalScrollView);
        demoList = (LinearLayout) root.findViewById(R.id.demoList);
        demoDescription = (TextView) root.findViewById(R.id.demoDescription);
        materialButtonShare = (MaterialButton) root.findViewById(R.id.shareNow);

        root.findViewById(R.id.backgroundFull).setBackgroundColor(TimeCheckMap.Companion.getTime().equals("day") ? context.getColor(R.color.light) : context.getColor(R.color.dark));

        onManagerReady((BillingProvider) activity);

        if (!functionsClass.cloudBackupSubscribed()) {
            demoDescription.setText(Html.fromHtml(getString(R.string.cloudBackupDemoDescriptions)));
            demoDescription.setTextColor(TimeCheckMap.Companion.getTime().equals("day") ? context.getColor(R.color.dark) : context.getColor(R.color.light));

            final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default);
            firebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    demoDescription.setText(Html.fromHtml(firebaseRemoteConfig.getString("cloud_backup_description")));
                    screenshotsNumber = (int) firebaseRemoteConfig.getLong("cloud_backup_demo_screenshots");

                    for (int i = 1; i <= screenshotsNumber; i++) {
                        String sceenshotFileName = "CloudBackupDemo" + i + ".png";
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                        StorageReference firebaseStorageReference = firebaseStorage.getReference();
                        StorageReference storageReference = firebaseStorageReference
                                //gs://....appspot.com/Assets/Images/Screenshots/CloudBackup/CloudBackupDemo1.png
                                .child("Assets/Images/Screenshots/CloudBackup/IAP.Demo/" + sceenshotFileName);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri screenshotURI) {
                                requestManager.load(screenshotURI)
                                        .addListener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                glideLoadCounter++;

                                                String beforeToken = screenshotURI.toString().split("\\?alt=media&token=")[0];
                                                int drawableIndex = Integer.parseInt(String.valueOf(beforeToken.charAt(beforeToken.length() - 5)));

                                                mapIndexDrawable.put(drawableIndex, resource);
                                                mapIndexURI.put(drawableIndex, screenshotURI);

                                                if (screenshotsNumber == glideLoadCounter) {
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            context.sendBroadcast(new Intent("LOAD_SCREENSHOTS"));
                                                        }
                                                    }, 113);
                                                }

                                                return false;
                                            }
                                        })
                                        .submit();
                            }
                        });
                    }
                }
            });

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("LOAD_SCREENSHOTS");
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("LOAD_SCREENSHOTS")) {
                        for (int i = 1; i <= screenshotsNumber; i++) {
                            FunctionsClassDebug.Companion.PrintDebug(">>> " + mapIndexURI.get(i) + " <<<");

                            RelativeLayout demoLayout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.iap_demo_item, null);
                            ImageView demoItem = (ImageView) demoLayout.findViewById(R.id.demoItem);

                            demoItem.setImageDrawable(mapIndexDrawable.get(i));
                            demoItem.setOnClickListener(AcquireFragment.this);
                            demoItem.setTag(mapIndexURI.get(i));
                            demoList.addView(demoLayout);
                        }
                    }
                }
            };
            context.registerReceiver(broadcastReceiver, intentFilter);
        }

        materialButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareText =
                        getString(R.string.shareTitle) +
                                "\n" + getString(R.string.shareSummary) +
                                "\n" + getString(R.string.play_store_link) + getContext().getPackageName();

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sharingIntent.setType("text/plain");
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sharingIntent);
            }
        });

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    requestManager.pauseAllRequests();
                    activity.finish();
                }
                return true;
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            requestManager.resumeRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requestManager.pauseAllRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof ImageView) {
            String screenshotURI = view.getTag().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(screenshotURI));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void refreshUI() {
        if (skusAdapter != null) {
            skusAdapter.notifyDataSetChanged();
        }
    }

    public void onManagerReady(BillingProvider billingProvider) {
        this.billingProvider = billingProvider;
        if (recyclerView != null) {
            skusAdapter = new SkusAdapter(this.billingProvider, activity);
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(skusAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            }
            handleManagerAndUiReady();
        }
    }

    private void handleManagerAndUiReady() {
        final List<SkuRowData> skuRowDataList = new ArrayList<>();

        List<String> subsSkus = billingProvider.getBillingManager().getSkus(BillingClient.SkuType.SUBS);
        billingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.SUBS,
                subsSkus,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                FunctionsClassDebug.Companion.PrintDebug("***** SUBS SKU List ::: " + skuDetails + " ***");
                                if (skuDetails.getSku().equals("cloud.backup") && functionsClass.cloudBackupSubscribed()) {

                                    continue;
                                }

                                if (skuDetails.getSku().equals("donation.subscription") && functionsClass.alreadyDonated()) {

                                    continue;
                                }

                                skuRowDataList.add(new SkuRowData(
                                        skuDetails,
                                        skuDetails.getSku(),
                                        skuDetails.getTitle(),
                                        skuDetails.getPrice(),
                                        skuDetails.getDescription(),
                                        skuDetails.getType())
                                );
                            }

                            if (skuRowDataList.size() > 0) {
                                skusAdapter.updateData(skuRowDataList);
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {

                            }
                        }
                    }
                });
    }

    private void displayError() {

    }
}

