package com.smallplanet.labainaction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.smallplanet.labalib.android.laba
import kotlinx.android.synthetic.main.activity_multielement_animation.*

class MultielementAnimation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multielement_animation)

        startAnimation()
    }

    fun startAnimation() {
        for (index in 0..container.childCount - 1) {
            container.getChildAt(index).laba("pD${1 + 0.25f*index}d1|>1000e7d1")
        }
    }
}
