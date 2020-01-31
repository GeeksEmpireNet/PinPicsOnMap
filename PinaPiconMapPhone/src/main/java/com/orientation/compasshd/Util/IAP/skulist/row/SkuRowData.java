/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */
package com.orientation.compasshd.Util.IAP.skulist.row;

import com.android.billingclient.api.SkuDetails;

/**
 * A model for SkusAdapter's row which holds all the data to render UI
 */
public class SkuRowData {
    SkuDetails skuDetails;
    final String sku, title, price, description, billingType;

    public SkuRowData(SkuDetails skuDetails, String sku, String title, String price, String description, String type) {
        this.skuDetails = skuDetails;
        this.sku = sku;
        this.title = title;
        this.price = price;
        this.description = description;
        this.billingType = type;
    }

    public SkuDetails getSkuDetails() {
        return this.skuDetails;
    }

    public String getSku() {
        return sku;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getBillingType() {
        return billingType;
    }
}
