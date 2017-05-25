package com.smallplanet.laba

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNewOperators()
        startAnimations()
    }

    fun startAnimations() {
        targetPink.laba("^200e10D1d1|c10e1d0.25|C10e1d0.25|D0.5s7f0!p30^100")
    }
}
