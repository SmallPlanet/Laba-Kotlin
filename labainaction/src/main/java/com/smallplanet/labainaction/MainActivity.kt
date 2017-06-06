package com.smallplanet.labainaction

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.smallplanet.labainaction.extras.addNewInterpolators
import com.smallplanet.labainaction.extras.addNewOperators
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNewOperators()
        addNewInterpolators()

        setEvents()
    }

    fun setEvents() {
        multiviewopen.setOnClickListener {
            val intent = Intent(this, MultielementAnimation::class.java)
            startActivity(intent)
        }
    }

    fun startAnimations() {
        //targetPink.laba("^100e11D1d1|c5e1d0.25|C5e1d0.25|D0.5s3f0!p30^100")
        //targetPink.laba("D1^80|>80|v160|<160|^160|>80|v80|r")
    }
}
