package com.smallplanet.labainaction

import android.view.View
import com.smallplanet.labalib.laba

/**
 * Created by javiermoreno on 6/6/17.
 */

class Dot(val back: View, val front: View) {

    var isOn = false

    init {
        front.laba("p90d0")
    }

    fun turnOn() {
        if (isOn)
            return

        turn(front, back)

        isOn = true
    }

    fun turnOff() {
        if (!isOn)
            return

        turn(back, front)

        isOn = false
    }

    private fun turn(b: View, f: View) {
        f.laba("p90d0.20", completeAction = {
            b.laba("p90d0.20")
        })
    }
}
