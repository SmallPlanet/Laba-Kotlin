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
        targetPink.laba("vp|>y|^p|<y|fr")

        targetBlue.laba("^p|<y|vp|>y|fr")
    }
}
