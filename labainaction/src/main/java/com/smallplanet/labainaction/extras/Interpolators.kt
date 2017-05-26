package com.smallplanet.labainaction.extras

import android.animation.TimeInterpolator
import com.smallplanet.labalib.LabaNotation

/**
 * Created by javiermoreno on 5/26/17.
 */

fun addNewInterpolators() {
    LabaNotation.addInterpolator(TimeInterpolator { input ->
        val x = 2.0f * input - 1.0f
        0.5f * (x * x * x + 1.0f)
    })
}