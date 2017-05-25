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
        val labaExpression = "^100e10D1d1|c5e1d0.25|C5e1d0.25|D0.5s3f0!p30^100|^100e10D1d1|c5e1d0.25|C5e1d0.25|D0.5s3f0!p30^100"

        expression.text = labaExpression
        description.text = targetPink.laba(labaExpression, true)
    }
}
