package com.smallplanet.labainaction

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

fun View.laba(notation: String, returnDescriptin: Boolean = false): String? {
    val labanotation = LabaNotation(notation, this)
    labanotation.animate()

    if (returnDescriptin)
        return labanotation.describe()

    return null
}

class LabaNotation(var notation: String, val view: View) {

    var masterAnimatorSet = AnimatorSet()

    init {
        notation = notation.replace(" ", "")
        masterAnimatorSet = processNotation().first
    }

    fun describeSegment(segment: String, sb: StringBuilder) {
        var duration: Float? = null
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var relativeLoop: Int? = null
        var interpolator: TimeInterpolator? = null
        var invert = false
        var tempBuilder = StringBuilder("")

        val commitTempSegment = {

            if (delay != null)
                sb.append("will wait for $delay seconds, once complete then ")

            sb.append(tempBuilder)

            if (interpolator != null)
                sb.append("with interpolator ${interpolator!!.javaClass.simpleName}, ")

            if(absoluteLoop != null || relativeLoop != null) {
                val loop = if (absoluteLoop != null) absoluteLoop else relativeLoop
                val loopType = if (absoluteLoop != null) "absolute repeat" else "reverse repeat"
                val loopNumber: String
                if(loop != -1)
                    loopNumber = (if (loop != 0) (loop!! - 1) else loop).toString()
                else
                    loopNumber = "forever"

                sb.append("$loopType repeating $loopNumber, ")
            }

            sb.append("over ${duration ?: LabaNotation.defaultDuration} seconds.")
        }

        val clearTempSegment= {
            duration = null
            delay = null
            interpolator = null
            absoluteLoop = null
            invert = false
            tempBuilder = StringBuilder("")
        }

        var i = 0

        while (i < segment.length) {
            val char = segment[i]

            if (char == '|') {
                commitTempSegment()
                clearTempSegment()
            }

            if(LabaNotation.operators.containsKey(char.toString())) {

                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                LabaNotation.operators[char.toString()]?.describe?.invoke(tempBuilder, view, param, null, invert)
            }

            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.getInterpolator(param.toInt())
                        'L' -> absoluteLoop = param.toInt()
                        'l' -> relativeLoop = param.toInt()
                    }

                }
            }

            invert = char == '!'
            i++
        }

        if (!segment.isEmpty()) {
            commitTempSegment()
            clearTempSegment()
        }
    }

    fun describe(): String {
        val notationToDescribe = notation.replace(" ", "")

        if(notationToDescribe.isEmpty())
            return "do nothing."

        val sb = StringBuilder("")

        if (notationToDescribe.contains('[')) {
            val segments = notationToDescribe.replace("[", "").split(']')
            var segmentNumber = 0

            sb.append("Perform a series of animations at the same time.\n")
            for (segment in segments) {
                if(segment.isNotEmpty()) {
                    sb.append ("Animation #${segmentNumber + 1} will ")
                    describeSegment (segment, sb)
                    sb.append ("\n")
                    segmentNumber++
                }
            }
        } else {
            describeSegment (notationToDescribe, sb)
        }

        return sb.toString().replace("  ", " ").capitalize()
    }

    private fun processNotation(index: Int = 0): Pair<AnimatorSet, Int> {
        var duration: Float = LabaNotation.defaultDuration
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var relativeLoop: Int? = null
        var interpolator: TimeInterpolator? = null
        var invert = false

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

            if(absoluteLoop != null || relativeLoop != null) {
                val loop = if (absoluteLoop != null) absoluteLoop else relativeLoop
                animators
                        .filterIsInstance<ValueAnimator>()
                        .forEach {
                            if(loop != -1)
                                it.repeatCount = if (loop != 0) (loop!! - 1) else loop
                            else
                                it.repeatCount = ValueAnimator.INFINITE

                            it.repeatMode = if (absoluteLoop != null) ValueAnimator.RESTART else ValueAnimator.REVERSE
                        }
            }

            animatorSet.playTogether(animators)

            if (interpolator != null)
                animatorSet.interpolator = interpolator

            animatorSet.duration = (duration * 1000).toLong()

            if (delay != null)
                animatorSet.startDelay = (delay!! * 1000).toLong()

            tempAnimators.add(animatorSet)
        }

        val clearTempAnimators = {
            animatorSet = AnimatorSet()
            duration = LabaNotation.defaultDuration
            delay = null
            interpolator = null
            absoluteLoop = null
            invert = false

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

                animators.add(LabaNotation.operators[char.toString()]?.animator?.invoke(view, param, invert))
            }

            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.getInterpolator(param.toInt())
                        'L' -> absoluteLoop = param.toInt()
                        'l' -> relativeLoop = param.toInt()
                    }

                }
            }

            invert = char == '!'
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

            if(!char.isDigit() && char != '.' && char != '-')
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
        val defaultDuration: Float = 0.75f

        val interpolators = arrayOf(LinearInterpolator(),                   //0
                LinearOutSlowInInterpolator(),          //1
                FastOutLinearInInterpolator(),          //2
                FastOutSlowInInterpolator(),            //3
                AccelerateInterpolator(),               //4
                DecelerateInterpolator(),               //5
                AccelerateDecelerateInterpolator(),     //6
                AnticipateInterpolator(),               //7
                OvershootInterpolator(),                //8
                AnticipateOvershootInterpolator(),      //9
                BounceInterpolator()                    //10

        )


        val controlOperators = arrayOf('|', '[', ']')
        val operators = mutableMapOf<String,LabaOperator>()

        init {
            addLabaOperator {
                symbol = "<"
                animator = {
                    view, param, invert ->
                    val localParam = (if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)).toPx

                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                        if(!invert)
                            sb.append("move left ${param ?: defaultParam} units, ")
                        else
                            sb.append("move in from left ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = ">"
                animator = {
                    view, param, invert ->
                    val localParam = (if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)).toPx

                    val originalPositon: Float by lazy { view.x }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.x = originalPositon + localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                        if(!invert)
                            sb.append("move right ${param ?: defaultParam} units, ")
                        else
                            sb.append("move in from right ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "^"
                animator = {
                    view, param, invert ->
                    val localParam = (if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)).toPx

                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("move up ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from above ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "v"
                animator = {
                    view, param, invert ->
                    val localParam = (if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)).toPx

                    val originalPositon: Float by lazy { view.y }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.y = originalPositon + localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("move down ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from below ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            addLabaOperator {
                symbol = "f"
                animator = {
                    view, param, invert ->
                    val localParam = 1 - if (invert) ((param ?: defaultParam) + 1) else (param ?: defaultParam)

                    val originalAlpha: Float by lazy { view.alpha }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.alpha = originalAlpha - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, view, param, _, invert ->
                    val originalAlpha: Float by lazy { view.alpha }

                    if(!invert)
                        sb.append("fade to ${(param ?: defaultParam) * 100}%, ")
                    else
                        sb.append("fade from $originalAlpha to ${(param ?: defaultParam) * 100}%, ")
                }
                defaultParam = 0f
            }

            addLabaOperator {
                symbol = "r"
                animator = {
                    view, param, invert ->
                    val localParam = if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)

                    val originalRotation: Float by lazy { view.rotation }
                    val animator = ValueAnimator.ofFloat(0f, 1f)

                    animator.addUpdateListener {
                        animation ->
                        view.rotation = originalRotation - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("rotate around z by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around z by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            addLabaOperator {
                symbol = "p"
                animator = {
                    view, param, invert ->
                    val localParam = if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)

                    val originalRotation: Float by lazy { view.rotationX }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.rotationX = originalRotation - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("rotate around x by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around x by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            addLabaOperator {
                symbol = "y"
                animator = {
                    view, param, invert ->
                    val localParam = if (invert) -1 * (param ?: defaultParam) else (param ?: defaultParam)

                    val originalRotation: Float by lazy { view.rotationY }
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.rotationY = originalRotation - localParam * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("rotate around y by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around y by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            addLabaOperator {
                symbol = "s"
                animator = {
                    view, param, invert ->
                    val localParam = if (invert) 1 / (param ?: defaultParam) else (param ?: defaultParam)

                    val originalScale: Pair<Float, Float> by lazy { Pair(view.scaleX, view.scaleY) }
                    val toScaleX: Float by lazy { originalScale.first - localParam }
                    val toScaleY: Float by lazy { originalScale.second - localParam }

                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.addUpdateListener {
                        animation ->
                        view.scaleX = originalScale.first - toScaleX * animation.animatedValue as Float
                        view.scaleY = originalScale.second - toScaleY * animation.animatedValue as Float
                    }
                    animator
                }
                describe = {
                    sb, _, param, _, invert ->
                    if(!invert)
                        sb.append("scale to ${(param ?: defaultParam) * 100}%, ")
                    else
                        sb.append("scale to ${((1 / (param ?: defaultParam)) * 100).format(1)}%, ")
                }
                defaultParam = 1f
            }

        }

        inline fun addLabaOperator(initialize: LabaOperator.() -> Unit) {
            val newLabaOperator = LabaOperator()
            newLabaOperator.initialize()

            newLabaOperator.symbol?.let {
                operators[it] = newLabaOperator
            }

        }

        fun getInterpolator(param: Int) = interpolators[param]
    }
}

class LabaOperator(var symbol: String? = null, var defaultParam: Float = 0f, var animator: ((View, Float?, Boolean) -> Animator)? = null, var describe: ((StringBuilder, View, Float?, Float?, Boolean) -> Unit)? = null)
