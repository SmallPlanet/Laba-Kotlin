package com.smallplanet.labainaction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.smallplanet.labalib.laba
import kotlinx.android.synthetic.main.activity_move_rotation.*

class MoveRotation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_rotation)

        startAnimation()
    }

    fun startAnimation() {
        targetPink.laba("D1^80|>80|v160|<160|^160|>80|v80|r")
    }
}
