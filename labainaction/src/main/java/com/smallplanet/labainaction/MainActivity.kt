package com.smallplanet.labainaction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.smallplanet.labainaction.extras.addNewInterpolators
import com.smallplanet.labainaction.extras.addNewOperators
import com.smallplanet.labalib.laba
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNewOperators()
        addNewInterpolators()
        startAnimations()
    }

    fun startAnimations() {
        val labaExpression = "^100e11D1d1|c5e1d0.25|C5e1d0.25|D0.5s3f0!p30^100"

        expression.text = labaExpression
        description.text = targetPink.laba(labaExpression, true)
    }
}
