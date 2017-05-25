package com.smallplanet.laba

import android.content.res.Resources

/**
 * Created by javiermoreno on 5/23/17.
 */

val Float.toDp: Float
    get(){
        return (this - 0.5f) / Resources.getSystem().displayMetrics.density
    }

val Int.toDp: Int
    get(){
        return ((this - 0.5f) / Resources.getSystem().displayMetrics.density).toInt()
    }

val Float.toPx: Float
    get(){
        return Resources.getSystem().displayMetrics.density * this + 0.5f
    }

val Int.toPx: Int
    get(){
        return (Resources.getSystem().displayMetrics.density * this + 0.5f).toInt()
    }

fun Number.format(digits: Int) = java.lang.String.format("%.${digits}f", this)