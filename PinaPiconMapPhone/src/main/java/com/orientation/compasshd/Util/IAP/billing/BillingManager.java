/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */
package com.orientation.compasshd.Util.IAP.billing;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.orientation.compasshd.Util.Functions.FunctionsClass;
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug;
import com.orientation.compasshd.Util.IAP.InAppBilling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";

    FunctionsClass functionsClass;

    private BillingClient billingClient;
    private Activity activity;

    String UserEmailAddress = null;

    private static final HashMap<String, List<String>> SKUS;

    static {
        SKUS = new HashMap<>();
        SKUS.put(BillingClient.SkuType.SUBS, Arrays.asList("donation.subscription", "cloud.backup"));
    }

    public List<String> getSkus(@BillingClient.SkuType String type) {
        return SKUS.get(type);
    }

    public BillingManager(Activity activity, String UserEmailAddress) {

        this.activity = activity;
        this.UserEmailAddress = UserEmailAddress;

        functionsClass = new FunctionsClass(activity);

        billingClient = BillingClient.newBuilder(this.activity).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<Purchase> purchasesSubscriptions = billingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList();
                    for (Purchase purchase : purchasesSubscriptions) {
                        FunctionsClassDebug.Companion.PrintDebug("*** Purchased Item: " + purchase + " ***");
                        functionsClass.savePreference(".SubscribedItem", purchase.getSku(), true);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    public BillingResult startPurchaseFlow(SkuDetails skuDetails, String skuId, String billingType) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .setAccountId(UserEmailAddress)
                .build();

        return billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //ResponseCode 7 = Item Owned
        Log.d(TAG, "onPurchasesUpdated() Response: " + billingResult.getResponseCode());

        activity.finish();
        activity.startActivity(new Intent(activity.getApplicationContext(), InAppBilling.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType, final List<String> skuList, final SkuDetailsResponseListener listener) {
        SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(itemType).build();
        billingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                listener.onSkuDetailsResponse(billingResult, skuDetailsList);
            }
        });
    }
}
