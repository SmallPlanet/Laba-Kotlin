package com.smallplanet.labainaction

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.smallplanet.labalib.laba
import kotlinx.android.synthetic.main.activity_count_down.*

class CountDown : AppCompatActivity() {

    var dotsMatrix: DotMatrix? = null
    private var runnable: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)

        initialize()
        startAnimation()

    }

    fun initialize() {
        val dotsData: MutableList<MutableList<Dot>> = arrayListOf()
        val rows = mainContainer.childCount

        for (i in 0..rows - 1)
            dotsData.add(arrayListOf())

        for (i in 0..rows - 1) {
            val row = (mainContainer.getChildAt(i) as LinearLayout)
            val cols = row.childCount
            (0..cols - 1)
                    .map { row.getChildAt(it) as RelativeLayout }
                    .forEach {
                        it.getChildAt(0).laba("D1s0.5d0.7e4|s1d0.5")
                        dotsData[i].add(Dot(it.getChildAt(0), it.getChildAt(1)))
                    }
        }

        dotsMatrix = DotMatrix(dotsData)
    }

    fun startAnimation() {
        val handler = Handler(Looper.getMainLooper())

        var count = 9
        runnable = {
            dotsMatrix?.turnOff()

            Handler(Looper.getMainLooper()).postDelayed({
                dotsMatrix?.turnOnNumber(count)

                count--
                if(count >= 0)
                    handler.postDelayed(runnable, 1000)

            }, 500)


            Unit
        }

        handler.postDelayed(runnable, 3000)
    }
}
