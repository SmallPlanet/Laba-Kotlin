package com.smallplanet.labalib

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.View
import android.view.animation.*


/**
 * Created by javiermoreno on 5/18/17.
 * Terms:
 * 1- Laba notation/expression: string that represents the steps to be performed in the coreography
 * 2- Segment: part of the notation that is contained between "[]"or an expression without brackets (in that case it would be just one segment), an expression by definition is a segment
 * 3- Sequence: pieces of the segment that are divided by "|"
 */

/**
 * This extension function provides quick access to the use of the laba notation.
 *
 * @param [notation] expression that represents the coreography to be performed by this view.
 * @param [returnDescriptin] specify if a return value is expected that contains the description of the specified [notation], doing this adds overhead to the processing of the [notation] this was created for debug purposes. Default value is false.
 * @param [completeAction] action to be executed at the end of the animation. Default value is null.
 * @return a string with the description of the coreography to be performed by the view, this return value is null (not computed) if the [returnDescriptin] parameter is false or not specified.
 */
fun View.laba(notation: String, returnDescriptin: Boolean = false, completeAction: ((Animator?) -> Unit)? = null): String? {
    val labanotation = LabaNotation(notation, this, completeAction)
    labanotation.animate()

    if (returnDescriptin)
        return labanotation.describe()

    return null
}

/**
 * LabaNotation class
 *
 * This class provides the core functionalities for the laba animations. This animatinons consist of a group of steps that define a coreography. Uses dp as units.
 *
 * `<` move left
 *
 * `>` move right
 *
 * `^` move up
 *
 * `v` move down
 *
 * `f` alpha fade
 *
 * `s` uniform scale
 *
 * `w` width
 *
 * `h` height
 *
 * `r` roll / z rotation
 *
 * `p` pitch / x rotation
 *
 * `y` yaw / y rotation
 *
 * `e` easing function (environment specific)
 *
 * `d` duration of sequence
 *
 * D` staggaered duration based on sibling/child index
 *
 * `L` loop (absolute) this sequence (value is number of times to loop, -1 means loop infinitely)
 *
 * `l` loop (reverse) this sequence (value is number of times to loop, -1 means loop infinitely)
 *
 * `|` pipe animations into multiple sequences
 *
 * `!` invert the next operator
 *
 * `[]` concurrent Laba animations aka segments ( example: [>d2][!fd1] )
 *
 * Available interpoaltors and their indexes to be used with the operator e
 *
 * `LinearInterpolator`                   - 0
 *
 * `LinearOutSlowInInterpolator`          - 1
 *
 * `FastOutLinearInInterpolator`          - 2
 *
 * `FastOutSlowInInterpolator`            - 3
 *
 * `AccelerateInterpolator`               - 4
 *
 * `DecelerateInterpolator`               - 5
 *
 * `AccelerateDecelerateInterpolator`     - 6
 *
 * `AnticipateInterpolator`               - 7
 *
 * `OvershootInterpolator`                - 8
 *
 * `AnticipateOvershootInterpolator`      - 9
 *
 * `BounceInterpolator`.                  - 10
 *
 * @param [notation] expression that represents the coreography to be performed by the specified view.
 * @param [view] View that its going to perform the animation.
 * @param [completeAction] action to be executed at the end of the animation. Default value is null.
 * @constructor Creates an object that encapsulates the functionalities of the laba coreography system.
 */
class LabaNotation(private var notation: String, private val view: View, private val completeAction: ((Animator?) -> Unit)? = null) {

    //This animator is going to contain all the animators created for the coreography, it doesn't specify an animation perse, it just the master switch
    private var masterAnimatorSet = android.animation.AnimatorSet()

    init {
        //Doing some cleaning in the notation to remove unwanted spaces
        notation = notation.replace(" ", "")
        //storing the processnotation in the master animator
        masterAnimatorSet = processNotation().first
        //Setting a listener to call the onComplete specified on the constructor
        masterAnimatorSet.addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) { completeAction?.invoke(animation) }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
    }

    /**
     * Function that is going to describe one segment (part of animation defined between []) of the animation
     *
     * @param [segment] Segment of animation to be described.
     * @param [sb] String builder for efficient string concatenation.
     */
    private fun describeSegment(segment: String, sb: StringBuilder) {

        //All this variable hold the current state of a sequence (parts of the segment separated by |)
        var duration: Float? = null
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var reverseLoop: Int? = null
        var interpolator: android.animation.TimeInterpolator? = null
        var invert = false
        var tempBuilder = StringBuilder("")

        //Its going to write down the current sequence
        //TODO: should refactor the name of this lambda to subsegment
        val commitTempSequence = {

            //Checking all the state variables set during the parsing of the current sequence
            if (delay != null)
                sb.append("will wait for $delay seconds, once complete then ")

            sb.append(tempBuilder)

            if (interpolator != null)
                sb.append("with interpolator ${interpolator!!.javaClass.simpleName}, ")

            //Dealing with loops
            //TODO: I think this should be refactored to make it more clear
            if(absoluteLoop != null || reverseLoop != null) {
                val loop = if (absoluteLoop != null) absoluteLoop else reverseLoop
                val loopType = if (absoluteLoop != null) "absolute repeat" else "reverse repeat"
                val loopNumber: String
                if(loop != -1)
                    loopNumber = (if (loop != 0) (loop!! - 1) else loop).toString()
                else
                    loopNumber = "forever"

                sb.append("$loopType repeating $loopNumber, ")
            }

            sb.append("over ${duration ?: LabaNotation.defaultDuration} seconds. ")
        }

        //Clearing sequence
        //TODO: should refactor the name of this lambda to subsegment
        val clearTempSequence= {
            duration = null
            delay = null
            interpolator = null
            absoluteLoop = null
            invert = false
            tempBuilder = StringBuilder("")
        }

        //Segment parsing
        var i = 0
        while (i < segment.length) {
            val char = segment[i]

            //Sequence done, is time to commit it and clear it
            if (char == '|') {
                commitTempSequence()
                clearTempSequence()
            }

            //Checking if the current char is an operator
            if(LabaNotation.operators.containsKey(char.toString())) {

                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                //Getting the description of the current operator
                LabaNotation.operators[char.toString()]?.describe?.invoke(tempBuilder, view, param, invert)
            }

            //State operators checking
            //TODO: this should be in an array or in constants, they shouldn't be just written down in the middle of the code
            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {
                    //Setting the state variable according to the operator
                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.getInterpolator(param.toInt())
                        'L' -> absoluteLoop = param.toInt()
                        'l' -> reverseLoop = param.toInt()
                    }

                }
            }

            //Knowing if the next operator needs to be inverted
            invert = char == '!'
            i++
        }

        //Commiting the last sequence
        commitTempSequence()
        clearTempSequence()
    }

    /**
     * Function that is going to describe the entire coreography
     */
    fun describe(): String {
        val notationToDescribe = notation.replace(" ", "")

        if(notationToDescribe.isEmpty())
            return "do nothing."

        val sb = StringBuilder("")

        //Checking if there are more than one segment
        if (notationToDescribe.contains('[')) {
            //Getting all the segments
            val segments = notationToDescribe.replace("[", "").split(']')
            var segmentNumber = 0

            //Iterating throug the segments and getting the description of each one of them
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
            //Just one segment that can be described immediately
            describeSegment (notationToDescribe, sb)
        }

        return sb.toString().replace("  ", " ").capitalizeSentences()
    }

    /**
     * This function is going to process the current laba expression
     *
     * @param [index] specifies from where to start parsing the expression, it is used for the recursive calls of the function, its default value is 0 to start from the beginning
     * @return the animator for the current parsed segment and the index of how far in the expression it went parsing, that second part of the pair is used for recursive calls
     */
    private fun processNotation(index: Int = 0): Pair<android.animation.AnimatorSet, Int> {
        //Variables that hold the state of the current subsegment
        var duration: Float = LabaNotation.defaultDuration
        var delay: Float? = null
        var absoluteLoop: Int? = null
        var reverseLoop: Int? = null
        var interpolator: android.animation.TimeInterpolator? = null
        var invert = false

        //AnimatorSet for the current sequence
        //this guy is going to control the animation of all the opeartors that are going to be stored in animators
        var sequenceAnimatorSet = android.animation.AnimatorSet()
        //Storage for the animator of each one of the operators in the current sequence
        val sequenceAnimators = mutableListOf<android.animation.Animator?>()
        //Contains the animators of each segment
        val segmentAnimatorSets = mutableListOf<android.animation.AnimatorSet>()

        //Creates an AnimatorSet with all the animatorSet captured by this segment
        val sendSegment: (List<android.animation.AnimatorSet>, Int) -> Pair<android.animation.AnimatorSet, Int> = {
            animators, index ->

            val resultAnimatorSet = android.animation.AnimatorSet()
            //This list is made of sequence so they are played sequentially
            resultAnimatorSet.playSequentially(animators)
            Pair(resultAnimatorSet, index)
        }

        //Saves all the animators for the current sequence
        val commitSequence = {

            //Checking if any of the state modifier needs to be applied to the animator set that controls this animators
            if(absoluteLoop != null || reverseLoop != null) {
                val loop = if (absoluteLoop != null) absoluteLoop else reverseLoop
                sequenceAnimators
                        .filterIsInstance<android.animation.ValueAnimator>()
                        .forEach {
                            if(loop != -1)
                                it.repeatCount = if (loop != 0) (loop!! - 1) else loop
                            else
                                it.repeatCount = android.animation.ValueAnimator.INFINITE

                            it.repeatMode = if (absoluteLoop != null) android.animation.ValueAnimator.RESTART else android.animation.ValueAnimator.REVERSE
                        }
            }

            //Since this are all inside of the same sequence the are going to be played together
            sequenceAnimatorSet.playTogether(sequenceAnimators)

            if (interpolator != null)
                sequenceAnimatorSet.interpolator = interpolator

            sequenceAnimatorSet.duration = (duration * 1000).toLong()

            if (delay != null)
                sequenceAnimatorSet.startDelay = (delay!! * 1000).toLong()

            segmentAnimatorSets.add(sequenceAnimatorSet)
        }

        //Clearing data for the current sequence to get ready to analyze the next sequence
        val clearSequence = {
            sequenceAnimatorSet = android.animation.AnimatorSet()
            duration = LabaNotation.defaultDuration
            delay = null
            interpolator = null
            absoluteLoop = null
            invert = false

            sequenceAnimators.clear()
        }

        var i = index

        while (i < notation.length) {
            //Getting the current token
            val char = notation[i]

            //New sequence found time to commit it
            if (char == '|') {
                commitSequence()
                clearSequence()
            }

            //Beginning of a new segment, so lets do a recursive call to capture this new segment
            if (char == '[') {
                //Storing the new segment as part of our sequence (it gets played together with the other concurrent segments)
                val (concurrentAnimator, newIndex) = processNotation(i + 1)
                sequenceAnimators.add(concurrentAnimator)
                i = newIndex
            }

            //for recursive call to stop
            if (char == ']') {
                break
            }

            //Checking if the current token is contained in our operators
            if(LabaNotation.operators.containsKey(char.toString())) {

                //Getting possible parameter for this operator
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                //moving the index past this parameter
                i = newIndex

                //Adding operator to our sequence
                sequenceAnimators.add(LabaNotation.operators[char.toString()]?.animator?.invoke(view, param, invert))
            }

            //State operators checking
            //TODO: this should be in an array or in constants, they shouldn't be just written down in the middle of the code
            if(char == 'D' || char == 'd' || char == 'e' || char == 'L' || char == 'l') {
                val (paramResult, newIndex) = getParameter(i)
                val param = paramResult
                i = newIndex

                if (param != null) {

                    //Setting state according to the operator
                    when (char) {
                        'd' -> duration = param
                        'D' -> delay = param
                        'e' -> interpolator = LabaNotation.getInterpolator(param.toInt())
                        'L' -> absoluteLoop = param.toInt()
                        'l' -> reverseLoop = param.toInt()
                    }

                }
            }

            //Capturing invert operator
            invert = char == '!'
            i++
        }

        //If there is any remaining sequece commit!
        if (!sequenceAnimators.isEmpty()) {
            commitSequence()
            clearSequence()
        }

        //Create animator set for this segment and update index to know how far the parsing went (recursive call)
        return sendSegment(segmentAnimatorSets, i)
    }

    /**
     * This function is going to return the parameter starting at the current index
     *
     * @param [index] specifies from where to start parsing the parameter
     * @return the parameter as a Float or null in case nothing needs to be parsed and the update index with the progress of the parsing
     */
    private fun getParameter(index: Int): Pair<Float?, Int> {
        var i = index
        val nextChar: Char? = if (i + 1 < notation.length) notation[i + 1] else null
        var param: Float? = null

        //Checking if the next char is not an operato, if thats the case the parameter gets parsed
        if (nextChar != null && !LabaNotation.operators.containsKey(nextChar.toString()) && !LabaNotation.controlOperators.contains(nextChar)) {
            val (paramResult, newIndex) = parseParam(i + 1)
            param = paramResult
            i = newIndex - 1
        }
        return Pair(param, i)
    }

    /**
     * This function is going to return the parameter starting at the current index, this function asumes that the paramenter is a number
     *
     * @param [index] specifies from where to start parsing the parameter
     * @return the parameter as a Float or null in case nothing needs to be parsed and the update index with the progress of the parsing
     */
    private fun parseParam(index: Int): Pair<Float?, Int> {
        var i = index

        //Getting when the number ends to parse it as a float
        while (i < notation.length) {
            val char = notation[i]

            if(!char.isDigit() && char != '.' && char != '-')
                break

            i++
        }

        val result = notation.substring(index, i).toFloatOrNull()

        //Returning the parsed float and the updated index
        return Pair(result, if(result == null) index else i)
    }

    /**
     * Performed the animation specified by the laba expression
     */
    fun animate() {
        masterAnimatorSet.start()
    }

    //Keeping all this data static because they dont need to be initialized more than once across different instances of the LabaNotation class
    companion object {
        private val defaultDuration: Float = 0.75f

        //List of the builtin interpolators and their respective indexes
        private val interpolators: MutableList<TimeInterpolator> = mutableListOf(
                LinearInterpolator(),                   //0
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


        private val controlOperators = arrayOf('|', '[', ']')
        private val operators = mutableMapOf<String, LabaOperator>()

        //Initialization of the builtin operators
        init {
            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

            LabaNotation.addLabaOperator {
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

        /**
         * Adds a new laba operator to the LabaNotation system. Use this function if you want to extend the LabaNotation with a new operator
         *
         * @param [initialize] initialization function which is a receiver of the LabaOperator.
         */
        fun addLabaOperator(initialize: LabaOperator.() -> Unit) {
            val newLabaOperator = LabaOperator()
            newLabaOperator.initialize()

            //Adding the operator ot the dictionary of operators using the symbol as the key
            newLabaOperator.symbol?.let {
                LabaNotation.operators[it] = newLabaOperator
            }

        }

        /**
         * Adds a new interpolator to the LabaNotation system. Use this function if you want to extend the LabaNotation with a new interpolator
         *
         * @param [interpolator] interpolator to be added to LabaNotation
         * @return the index for the new interpolator
         */
        fun addInterpolator(interpolator: TimeInterpolator): Int {
            interpolators.add(interpolator)
            return interpolators.size - 1
        }

        /**
         * Portal function to get an interpolator
         *
         * @param [index] of the interpolator to be returned, private use only
         * @return the interpolator in this position
         */
        private fun getInterpolator(index: Int): TimeInterpolator? = if(index >= interpolators.size) null else LabaNotation.interpolators[index]
    }
}

/**
 * Definition of a laba operator
 * @param [symbol] symbol that defines this operator
 * @param [defaultParam] default param in case that no paramater is not specified when this operator is used
 * @param [animator] animation performed by this operator
 * @param [describe] function to be called to get a description of what this operator does
 * @constructor Creates an object that encapsulates the functionalities of the laba coreography system.
 *
 */
class LabaOperator(var symbol: String? = null, var defaultParam: Float = 0f, var animator: ((View, Float?, Boolean) -> android.animation.Animator)? = null, var describe: ((StringBuilder, View, Float?, Boolean) -> Unit)? = null)

/*------------------------------------EXTENSIONS------------------------------------*/
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