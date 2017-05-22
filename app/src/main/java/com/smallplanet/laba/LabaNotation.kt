package com.smallplanet.laba

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator


/**
 * Created by javiermoreno on 5/18/17.
 */

fun View.laba(notation: String) {
    val labanotation = LabaNotation(notation, this)
    labanotation.animate()
}

class LabaNotation(val notation: String, val view: View) {

    val masterAnimatorSet = AnimatorSet()
    val animators = mutableListOf<AnimatorSet>()

    init {
        processNotation()
    }

    private fun processNotation() {
        var animatorSet = AnimatorSet()
        val animators = mutableListOf<Animator?>()

        val sendAnimators = {
            animatorSet.playTogether(animators)
            addToSequence(animatorSet)
        }

        val clearTempAnimators = {
            animatorSet = AnimatorSet()
            animators.clear()
        }

        for (char in notation) {

            if (char == '|') {
                sendAnimators()
                clearTempAnimators()
            }

            animators.add(LabaNotation.operators[char.toString()]?.animator?.invoke(view, null, null))
        }

        sendAnimators()
    }

    private fun addToSequence(animatorSet: AnimatorSet) {
        animators.add(animatorSet)
    }

    fun animate() {
        masterAnimatorSet.playSequentially(animators as List<Animator>?)
        masterAnimatorSet.start()
    }

    companion object {
        val operators = mutableMapOf<String,LabaOperator>()

        init {
            addLabaOperator {
                symbol = "<"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units to the left"
                }
                defaultDuration = 0.75f
                defaultParam = 500f
            }

            addLabaOperator {
                symbol = ">"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon + (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units to the right"
                }
                defaultDuration = 0.75f
                defaultParam = 500f
            }

            addLabaOperator {
                symbol = "^"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units up"
                }
                defaultDuration = 0.75f
                defaultParam = 500f
            }

            addLabaOperator {
                symbol = "v"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon + (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units down"
                }
                defaultDuration = 0.75f
                defaultParam = 500f
            }

            addLabaOperator {
                symbol = "f"
                animator = {
                    view, param, duration ->
                    val originalAlpha: Float by lazy { view.alpha }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.alpha = originalAlpha - (param ?: defaultParam) * animation.animatedValue as Float
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

            addLabaOperator {
                symbol = "r"
                animator = {
                    view, param, duration ->
                    val originalRotation: Float by lazy { view.rotation }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.rotation = originalRotation - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units down"
                }
                defaultDuration = 0.75f
                defaultParam = 360f
            }

            addLabaOperator {
                symbol = "p"
                animator = {
                    view, param, duration ->
                    val originalRotation: Float by lazy { view.rotationX }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.rotationX = originalRotation - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units down"
                }
                defaultDuration = 0.75f
                defaultParam = 360f
            }

            addLabaOperator {
                symbol = "y"
                animator = {
                    view, param, duration ->
                    val originalRotation: Float by lazy { view.rotationY }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.rotationY = originalRotation - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units down"
                }
                defaultDuration = 0.75f
                defaultParam = 360f
            }

        }

        fun addLabaOperator(initialize: LabaOperator.() -> Unit) {
            val newLabaOperator = LabaOperator()
            newLabaOperator.initialize()

            newLabaOperator.symbol?.let {
                operators[it] = newLabaOperator
            }

        }
    }
}

class LabaOperator(var symbol: String? = null, var defaultDuration: Float = 0.75f, var defaultParam: Float = 0f, var animator: ((View, Float?, Float?) -> Animator)? = null, var description: ((View, Float) -> String)? = null)
