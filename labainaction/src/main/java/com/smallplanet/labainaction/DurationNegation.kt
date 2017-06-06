package com.smallplanet.labainaction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.smallplanet.labalib.laba
import kotlinx.android.synthetic.main.activity_duration_negation.*

class DurationNegation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duration_negation)

        startAnimation()
    }

    fun startAnimation() {
        targetPink.laba("^100e11D1d1|c5e1d0.25|C5e1d0.25|D0.5s3f0!p30^100")
    }
}
