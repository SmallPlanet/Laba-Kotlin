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
                    var originalPositon = view.x
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            originalPositon = view.x
                        }

                    })
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
                    var originalPositon = view.x
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon + (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            originalPositon = view.x
                        }

                    })
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
                    var originalPositon = view.y
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            originalPositon = view.y
                        }

                    })
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
                    var originalPositon = view.y
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon + (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            originalPositon = view.y
                        }

                    })
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
                    var originalAlpha = view.alpha
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.interpolator = OvershootInterpolator()
                    animator.addUpdateListener {
                        animation ->
                        view.alpha = originalAlpha - (param ?: defaultParam) * animation.animatedValue as Float
                    }
                    animator.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            originalAlpha = view.alpha
                        }

                    })
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
