package com.smallplanet.laba

import android.animation.ValueAnimator

/**
 * Created by javiermoreno on 5/25/17.
 */

fun addNewOperators() {
    LabaNotation.addLabaOperator {
        symbol = "c"
        animator = {
            view, param, invert ->
            val localParam = if (invert) 1 / (param ?: defaultParam) else (param ?: defaultParam)

            val originalScale: Float by lazy { view.scaleX }
            val toScaleX: Float by lazy { originalScale - localParam }

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener {
                animation ->
                view.scaleX = originalScale - toScaleX * animation.animatedValue as Float
            }
            animator
        }
        describe = {
            sb, _, param, duration, invert ->
            if(!invert)
                sb.append("scale x to ${(param ?: defaultParam) * 100}%, ")
            else
                sb.append("scale x to ${((1 / (param ?: defaultParam)) * 100).format(1)}%, ")
        }
        defaultParam = 1f
    }


    LabaNotation.addLabaOperator {
        symbol = "C"
        animator = {
            view, param, invert ->
            val localParam = if (invert) 1 / (param ?: defaultParam) else (param ?: defaultParam)

            val originalScale: Float by lazy { view.scaleY }
            val toScaleY: Float by lazy { originalScale - localParam }

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener {
                animation ->
                view.scaleY = originalScale - toScaleY * animation.animatedValue as Float
            }
            animator
        }
        describe = {
            sb, _, param, duration, invert ->
            if(!invert)
                sb.append("scale y to ${(param ?: defaultParam) * 100}%, ")
            else
                sb.append("scale y to ${((1 / (param ?: defaultParam)) * 100).format(1)}%, ")
        }
        defaultParam = 1f
    }
}