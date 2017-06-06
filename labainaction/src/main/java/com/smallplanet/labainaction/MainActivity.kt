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

        moverotation.setOnClickListener {
            val intent = Intent(this, MoveRotation::class.java)
            startActivity(intent)
        }

        durationnegation.setOnClickListener {
            val intent = Intent(this, DurationNegation::class.java)
            startActivity(intent)
        }

        countdown.setOnClickListener {
            val intent = Intent(this, CountDown::class.java)
            startActivity(intent)
        }
    }
}
