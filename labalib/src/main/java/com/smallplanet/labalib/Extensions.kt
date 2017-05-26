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

fun String.capitalizeSentences(): String {
    var pos = 0
    var capitalize = true
    val sb = StringBuilder(this)
    while (pos < sb.length) {
        if (sb[pos] == '.') {
            capitalize = true
        } else if (capitalize && !Character.isWhitespace(sb[pos])) {
            sb.setCharAt(pos, Character.toUpperCase(sb[pos]))
            capitalize = false
        }
        pos++
    }

    return sb.toString()
}