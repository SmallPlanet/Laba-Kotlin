package com.smallplanet.labalib

import android.content.res.Resources

/**
 * Created by javiermoreno on 5/23/17.
 */

/**
 *For internal use only this is an extension function that converts from dp to pixels
 */
internal val Float.toPx: Float
    get(){
        return Resources.getSystem().displayMetrics.density * this + 0.5f
    }

/**
 * Returns a formatted version of the current number with the specified amount of digits after the dot for printing purposes
 *
 * @param [digits] number of digits after the dot
 */
internal fun Number.format(digits: Int) = java.lang.String.format("%.${digits}f", this)!!

/**
 * Capitalize each sentence in a string
 */
internal fun String.capitalizeSentences(): String {
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