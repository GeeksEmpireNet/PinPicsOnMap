/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.NavAdapter

import android.graphics.drawable.Drawable

class NavDrawerItem {

    internal var title: String
    internal var icon: Drawable
    internal var id: Int = 0

    constructor(title: String, icon: Drawable, id: Int) {
        this.title = title
        this.icon = icon
        this.id = id
    }

    fun getTitle(): String {
        return this.title
    }

    fun getIcon(): Drawable {
        return this.icon
    }

    fun getId(): Int {
        return this.id
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setIcon(icon: Drawable) {
        this.icon = icon
    }

    fun setId(Id: Int) {
        this.id = Id
    }
}