package com.smallplanet.labalib

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator


/**
 * Created by javiermoreno on 5/18/17.
 */

fun android.view.View.laba(notation: String, returnDescriptin: Boolean = false, completeAction: ((Animator?) -> Unit)? = null): String? {
    val labanotation = LabaNotation(notation, this, completeAction)
    labanotation.animate()

    if (returnDescriptin)
        return labanotation.describe()

    return null
}

class LabaNotation(private var notation: String, private val view: android.view.View, private val completeAction: ((Animator?) -> Unit)?) {

    private var masterAnimatorSet = android.animation.AnimatorSet()

    init {
        notation = notation.replace(" ", "")
        masterAnimatorSet = processNotation().first
        masterAnimatorSet.addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) = completeAction?.invoke(animation)!!

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
    }

    private fun describeSegment(segment: String, sb: StringBuilder) {
        var duration: Float? = null
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var relativeLoop: Int? = null
        var interpolator: android.animation.TimeInterpolator? = null
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

            sb.append("over ${duration ?: LabaNotation.Companion.defaultDuration} seconds. ")
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

            if(LabaNotation.Companion.operators.containsKey(char.toString())) {

                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                LabaNotation.Companion.operators[char.toString()]?.describe?.invoke(tempBuilder, view, param, invert)
            }

            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.Companion.getInterpolator(param.toInt())
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

        return sb.toString().replace("  ", " ").capitalizeSentences()
    }

    private fun processNotation(index: Int = 0): Pair<android.animation.AnimatorSet, Int> {
        var duration: Float = LabaNotation.Companion.defaultDuration
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var relativeLoop: Int? = null
        var interpolator: android.animation.TimeInterpolator? = null
        var invert = false

        var animatorSet = android.animation.AnimatorSet()
        val animators = mutableListOf<android.animation.Animator?>()
        val tempAnimators = mutableListOf<android.animation.AnimatorSet>()

        val sendAnimators: (List<android.animation.AnimatorSet>, Int) -> Pair<android.animation.AnimatorSet, Int> = {
            animators, index ->

            val resultAnimatorSet = android.animation.AnimatorSet()
            resultAnimatorSet.playSequentially(animators)
            Pair(resultAnimatorSet, index)
        }

        val commitTempAnimators = {

            if(absoluteLoop != null || relativeLoop != null) {
                val loop = if (absoluteLoop != null) absoluteLoop else relativeLoop
                animators
                        .filterIsInstance<android.animation.ValueAnimator>()
                        .forEach {
                            if(loop != -1)
                                it.repeatCount = if (loop != 0) (loop!! - 1) else loop
                            else
                                it.repeatCount = android.animation.ValueAnimator.INFINITE

                            it.repeatMode = if (absoluteLoop != null) android.animation.ValueAnimator.RESTART else android.animation.ValueAnimator.REVERSE
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
            animatorSet = android.animation.AnimatorSet()
            duration = LabaNotation.Companion.defaultDuration
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

            if(LabaNotation.Companion.operators.containsKey(char.toString())) {

                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                animators.add(LabaNotation.Companion.operators[char.toString()]?.animator?.invoke(view, param, invert))
            }

            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.Companion.getInterpolator(param.toInt())
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

        if (nextChar != null && !LabaNotation.Companion.operators.containsKey(nextChar.toString()) && !LabaNotation.Companion.controlOperators.contains(nextChar)) {
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

        val interpolators: MutableList<TimeInterpolator> = mutableListOf(android.view.animation.LinearInterpolator(),                   //0
                android.support.v4.view.animation.LinearOutSlowInInterpolator(),          //1
                android.support.v4.view.animation.FastOutLinearInInterpolator(),          //2
                android.support.v4.view.animation.FastOutSlowInInterpolator(),            //3
                android.view.animation.AccelerateInterpolator(),               //4
                android.view.animation.DecelerateInterpolator(),               //5
                android.view.animation.AccelerateDecelerateInterpolator(),     //6
                android.view.animation.AnticipateInterpolator(),               //7
                android.view.animation.OvershootInterpolator(),                //8
                android.view.animation.AnticipateOvershootInterpolator(),      //9
                android.view.animation.BounceInterpolator()                    //10

        )


        val controlOperators = arrayOf('|', '[', ']')
        val operators = mutableMapOf<String, LabaOperator>()

        init {
            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("move left ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from left ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("move right ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from right ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("move up ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from above ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("move down ${param ?: defaultParam} units, ")
                    else
                        sb.append("move in from below ${param ?: defaultParam} units, ")
                }
                defaultParam = 100f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, view, param, invert ->
                    val originalAlpha: Float by lazy { view.alpha }

                    if (!invert)
                        sb.append("fade to ${(param ?: defaultParam) * 100}%, ")
                    else
                        sb.append("fade from $originalAlpha to ${(param ?: defaultParam) * 100}%, ")
                }
                defaultParam = 0f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("rotate around z by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around z by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("rotate around x by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around x by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
                        sb.append("rotate around y by ${param ?: defaultParam}° units, ")
                    else
                        sb.append("rotate in from around y by ${param ?: defaultParam}° units, ")
                }
                defaultParam = 360f
            }

            LabaNotation.Companion.addLabaOperator {
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
                    sb, _, param, invert ->
                    if (!invert)
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
                LabaNotation.Companion.operators[it] = newLabaOperator
            }

        }

        fun addInterpolator(interpolator: TimeInterpolator): Int {
            interpolators.add(interpolator)
            return interpolators.size - 1
        }

        fun getInterpolator(index: Int): TimeInterpolator? = if(index >= interpolators.size) null else LabaNotation.Companion.interpolators[index]
    }
}

class LabaOperator(var symbol: String? = null, var defaultParam: Float = 0f, var animator: ((android.view.View, Float?, Boolean) -> android.animation.Animator)? = null, var describe: ((StringBuilder, android.view.View, Float?, Boolean) -> Unit)? = null)
