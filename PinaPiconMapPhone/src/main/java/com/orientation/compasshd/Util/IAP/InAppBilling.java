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

import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.orientation.compasshd.Maps.TimeCheckMap;
import com.orientation.compasshd.R;
import com.orientation.compasshd.Util.Functions.FunctionsClass;
import com.orientation.compasshd.Util.IAP.billing.BillingManager;
import com.orientation.compasshd.Util.IAP.billing.BillingProvider;


public class InAppBilling extends FragmentActivity implements BillingProvider {

    private static final String TAG = "InAppBilling";
    private static final String DIALOG_TAG = "InAppBillingDialogue";

    FunctionsClass functionsClass;

    private BillingManager billingManager;
    private AcquireFragment acquireFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TimeCheckMap.Companion.getTime().equals("day")) {
            setTheme(R.style.GeeksEmpire_Material_IAP_LIGHT);
        } else {
            setTheme(R.style.GeeksEmpire_Material_IAP_DARK);
        }

        functionsClass = new FunctionsClass(getApplicationContext());

        if (savedInstanceState != null) {
            acquireFragment = (AcquireFragment) getFragmentManager().findFragmentByTag(DIALOG_TAG);
        }

        billingManager = new BillingManager(InAppBilling.this, FirebaseAuth.getInstance().getCurrentUser().getEmail());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToPurchaseFragment();
            }
        }, 777);

        showRefreshedUi();
    }

    @Override
    public BillingManager getBillingManager() {
        return billingManager;
    }

    public void proceedToPurchaseFragment() {
        if (acquireFragment == null) {
            acquireFragment = new AcquireFragment();
        }

        if (!isAcquireFragmentShown()) {
            acquireFragment.show(getFragmentManager(), DIALOG_TAG);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    public void showRefreshedUi() {
        if (isAcquireFragmentShown()) {
            acquireFragment.refreshUI();
        }
    }

    public boolean isAcquireFragmentShown() {
        return acquireFragment != null && acquireFragment.isVisible();
    }
}
