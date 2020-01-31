/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.IAP.skulist;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.orientation.compasshd.R;
import com.orientation.compasshd.Util.IAP.billing.BillingProvider;
import com.orientation.compasshd.Util.IAP.skulist.row.RowViewHolder;
import com.orientation.compasshd.Util.IAP.skulist.row.SkuRowData;

import java.util.List;

public class SkusAdapter extends RecyclerView.Adapter<RowViewHolder> implements RowViewHolder.OnButtonClickListener {

    Activity activity;

    List<SkuRowData> rowDataList;
    BillingProvider billingProvider;

    public SkusAdapter(BillingProvider billingProvider, Activity activity) {
        this.billingProvider = billingProvider;
        this.activity = activity;
    }

    public void updateData(List<SkuRowData> skuRowData) {
        rowDataList = skuRowData;

        notifyDataSetChanged();
    }

    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.iap_sku_details_item, parent, false);
        return new RowViewHolder(inflate, this);
    }

    @Override
    public void onBindViewHolder(RowViewHolder rowViewHolder, int position) {
        SkuRowData skuRowData = getData(position);
        if (skuRowData != null) {
            rowViewHolder.purchaseItemName.setText(skuRowData.getTitle());
            rowViewHolder.purchaseItemDescription.setText(skuRowData.getDescription());
            rowViewHolder.purchaseItemButton.setEnabled(true);

            rowViewHolder.purchaseItemInfo.setVisibility(View.VISIBLE);
        }
        switch (skuRowData.getSku()) {
            case "cloud.backup": {
                rowViewHolder.purchaseItemPrice.setText(Html.fromHtml("<u>" + skuRowData.getPrice() + "</u>" + "<br/><small>Month</small>"));

                rowViewHolder.purchaseItemIcon.setImageResource(R.drawable.ic_launcher_round);
                rowViewHolder.purchaseItemButton.setText(activity.getString(R.string.subscribe));

                break;
            }
            case "donation.subscription": {
                rowViewHolder.purchaseItemPrice.setText(Html.fromHtml("<u>" + skuRowData.getPrice() + "</u>" + "<br/><small>Month</small>"));

                rowViewHolder.purchaseItemIcon.setImageResource(R.drawable.logo);
                rowViewHolder.purchaseItemButton.setText(activity.getString(R.string.donate));

                rowViewHolder.purchaseItemInfo.setText(R.string.thanks);

                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return rowDataList == null ? 0 : rowDataList.size();
    }

    @Override
    public void onButtonClicked(int position) {
        SkuRowData skuRowData = getData(position);
        billingProvider.getBillingManager().startPurchaseFlow(skuRowData.getSkuDetails(), skuRowData.getSku(), skuRowData.getBillingType());

    }

    public SkuRowData getData(int position) {
        return rowDataList == null ? null : rowDataList.get(position);
    }
}

