package com.smallplanet.labalib

import android.content.res.Resources

/**
 * Created by javiermoreno on 5/23/17.
 */

val Float.toPx: Float
    get(){
        return Resources.getSystem().displayMetrics.density * this + 0.5f
    }

fun Number.format(digits: Int) = java.lang.String.format("%.${digits}f", this)!!