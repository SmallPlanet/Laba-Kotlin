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
//        targetPink.laba("vpe10|>e10y|[^e10|r90e10d0.5|r90|r90|r90e10d0.5][<]|rfs")
//
//        targetBlue.laba("^200p180d1|<200d1y180|v200p180d2|>200y180|f0r490s4")
//
//        targetYellow.laba("^200p180d1D2|<200d1y180|v200p180d2|>200y180|f0r720s1")

        //targetYellow.laba("!r180e10")
        targetYellow.laba("f0.5|f0")
        targetPink.laba("f0.5|f0")
        targetBlue.laba("s2|s")
    }
}
