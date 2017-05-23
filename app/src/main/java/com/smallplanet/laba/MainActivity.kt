package com.smallplanet.laba

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            startAnimations()
        }, 1000)
    }

    fun startAnimations() {
        targetPink.laba("vp|>y|[^|r90|r90|r90|r90][<]|rfs")

        targetBlue.laba("^200p180d5|<200d5y180|v200p180d5|>200y180|f0.5r490s4")

        targetYellow.laba("r90.5D5v100d0.25")
    }
}
