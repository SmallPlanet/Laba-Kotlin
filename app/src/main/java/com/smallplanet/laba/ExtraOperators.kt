package com.smallplanet.laba

import android.animation.ValueAnimator

/**
 * Created by javiermoreno on 5/25/17.
 */

fun addNewOperators() {
    LabaNotation.addLabaOperator {
        symbol = "c"
        animator = {
            view, param, duration, invert ->
            val localParam = if (invert) 1 / (param ?: defaultParam) else (param ?: defaultParam)

            val originalScale: Float by lazy { view.scaleX }
            val toScaleX: Float by lazy { originalScale - localParam }

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = (duration ?: defaultDuration * 1000).toLong()
            animator.addUpdateListener {
                animation ->
                view.scaleX = originalScale - toScaleX * animation.animatedValue as Float
            }
            animator
        }
        description = {
            _, param ->
            "Its going to move the target $param units down"
        }
        defaultDuration = 0.75f
        defaultParam = 1f
    }


    LabaNotation.addLabaOperator {
        symbol = "C"
        animator = {
            view, param, duration, invert ->
            val localParam = if (invert) 1 / (param ?: defaultParam) else (param ?: defaultParam)

            val originalScale: Float by lazy { view.scaleY }
            val toScaleY: Float by lazy { originalScale - localParam }

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = (duration ?: defaultDuration * 1000).toLong()
            animator.addUpdateListener {
                animation ->
                view.scaleY = originalScale - toScaleY * animation.animatedValue as Float
            }
            animator
        }
        description = {
            _, param ->
            "Its going to move the target $param units down"
        }
        defaultDuration = 0.75f
        defaultParam = 1f
    }
}