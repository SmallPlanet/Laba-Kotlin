package com.smallplanet.laba

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View


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

    }

    private fun addToSequence(animatorSet: AnimatorSet) {
        animators.add(animatorSet)
    }

    fun animate() {
        masterAnimatorSet.playSequentially(animators as List<Animator>?)
        masterAnimatorSet.start()
    }
}

class LabaOperator(val value: String, val animatorProvider: (View) -> Animator, val getDescription: () -> String)
