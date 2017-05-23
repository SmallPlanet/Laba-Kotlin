package com.smallplanet.laba

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.View
import android.view.animation.*


/**
 * Created by javiermoreno on 5/18/17.
 */

fun View.laba(notation: String) {
    val labanotation = LabaNotation(notation, this)
    labanotation.animate()
}

class LabaNotation(val notation: String, val view: View) {

    var masterAnimatorSet = AnimatorSet()

    init {
        masterAnimatorSet = processNotation().first
    }

    private fun processNotation(index: Int = 0): Pair<AnimatorSet, Int> {
        var duration: Float? = null
        var delay: Float? = null
        var interpolator: TimeInterpolator? = null

        var animatorSet = AnimatorSet()
        val animators = mutableListOf<Animator?>()
        val tempAnimators = mutableListOf<AnimatorSet>()

        val sendAnimators: (List<AnimatorSet>, Int) -> Pair<AnimatorSet, Int> = {
            animators, index ->

            val resultAnimatorSet = AnimatorSet()
            resultAnimatorSet.playSequentially(animators)
            Pair(resultAnimatorSet, index)
        }

        val commitTempAnimators = {
            animatorSet.playTogether(animators)

            if (interpolator != null)
                animatorSet.interpolator = interpolator
            if (duration != null)
                animatorSet.duration = (duration!! * 1000).toLong()
            if (delay != null)
                animatorSet.startDelay = (delay!! * 1000).toLong()

            tempAnimators.add(animatorSet)
        }

        val clearTempAnimators = {
            animatorSet = AnimatorSet()
            duration = null
            delay = null
            interpolator = null

            animators.clear()
        }

        var i = index

        while (i < notation.length) {
            val char = notation[i]

            if (char == '|') {
                commitTempAnimators()
                clearTempAnimators()
            }

            if (char == '[') {
                val (concurrentAnimator, newIndex) = processNotation(i + 1)
                animators.add(concurrentAnimator)
                i = newIndex
            }

            if (char == ']') {
                break
            }

            if(LabaNotation.operators.containsKey(char.toString())) {

                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                animators.add(LabaNotation.operators[char.toString()]?.animator?.invoke(view, param, null))
            }

            if(char == 'D' || char == 'd' || char == 'e') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.getInterpolator(param.toInt())
                    }

                }
            }

            i++
        }

        if (!animators.isEmpty()) {
            commitTempAnimators()
            clearTempAnimators()
        }

        return sendAnimators(tempAnimators, i)
    }

    private fun getParameter(index: Int): Pair<Float?, Int> {
        var i = index
        val nextChar: Char? = if (i + 1 < notation.length) notation[i + 1] else null
        var param: Float? = null

        if (nextChar != null && !operators.containsKey(nextChar.toString()) && !controlOperators.contains(nextChar)) {
            val (paramResult, newIndex) = parseParam(i + 1)
            param = paramResult
            i = newIndex - 1
        }
        return Pair(param, i)
    }

    private fun parseParam(index: Int): Pair<Float?, Int> {
        var i = index

        while (i < notation.length) {
            val char = notation[i]

            if(!char.isDigit() && char != '.')
                break

            i++
        }

        val result = notation.substring(index, i).toFloatOrNull()

        return Pair(result, if(result == null) index else i)
    }

    fun animate() {
        masterAnimatorSet.start()
    }

    companion object {
        val interpolators = arrayOf(LinearInterpolator(),                   //0
                                    AccelerateDecelerateInterpolator(),     //1
                                    AccelerateInterpolator(),               //2
                                    AnticipateInterpolator(),               //3
                                    AnticipateOvershootInterpolator(),      //4
                                    BounceInterpolator(),                   //5
                                    DecelerateInterpolator(),               //6
                                    FastOutLinearInInterpolator(),          //7
                                    FastOutSlowInInterpolator(),            //8
                                    LinearOutSlowInInterpolator(),          //9
                                    OvershootInterpolator())                //10

        val controlOperators = arrayOf('|', '[', ']')
        val operators = mutableMapOf<String,LabaOperator>()

        init {
            addLabaOperator {
                symbol = "<"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon - (param ?: defaultParam).toPx * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units to the left"
                }
                defaultDuration = 0.75f
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = ">"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon + (param ?: defaultParam).toPx * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units to the right"
                }
                defaultDuration = 0.75f
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "^"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon - (param ?: defaultParam).toPx * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units up"
                }
                defaultDuration = 0.75f
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "v"
                animator = {
                    view, param, duration ->
                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon + (param ?: defaultParam).toPx * animation.animatedValue as Float
                    }
                    animator
                }
                description = {
                    _, param ->
                    "Its going to move the target $param units down"
                }
                defaultDuration = 0.75f
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "f"
                animator = {
                    view, param, duration ->
                    val originalAlpha: Float by lazy { view.alpha }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
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

            addLabaOperator {
                symbol = "s"
                animator = {
                    view, param, duration ->
                    val originalScale: Pair<Float, Float> by lazy { Pair(view.scaleX, view.scaleY) }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = (duration ?: defaultDuration * 1000).toLong()
                    animator.addUpdateListener {
                        animation ->
                        view.scaleX = originalScale.first + (param ?: defaultParam) * animation.animatedValue as Float
                        view.scaleY = originalScale.second + (param ?: defaultParam) * animation.animatedValue as Float
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

        fun addLabaOperator(initialize: LabaOperator.() -> Unit) {
            val newLabaOperator = LabaOperator()
            newLabaOperator.initialize()

            newLabaOperator.symbol?.let {
                operators[it] = newLabaOperator
            }

        }

        fun getInterpolator(param: Int) = interpolators[param]
    }
}

class LabaOperator(var symbol: String? = null, var defaultDuration: Float = 0.75f, var defaultParam: Float = 0f, var animator: ((View, Float?, Float?) -> Animator)? = null, var description: ((View, Float) -> String)? = null)
