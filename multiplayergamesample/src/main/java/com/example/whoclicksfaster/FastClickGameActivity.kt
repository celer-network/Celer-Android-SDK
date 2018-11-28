package com.example.whoclicksfaster

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import network.celer.celersdk.CAppCallback

class FastClickGameActivity : AppCompatActivity() {
    private val TAG = "who clicks faster"

    val MAX = 50

    var numberOfClicks = 0

    var lock = false

    var handler: Handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_click_game)


        CelerClientAPIHelper.initSession(this, GameGroupAPIHelper.gresp, callback)

        yourScoreBar.max = MAX
        opponentScoreBar.max = MAX
    }


    fun clickMe(v: View) {
        var state = ByteArray(1)
        numberOfClicks++
        state[0] = numberOfClicks.toByte()

        CelerClientAPIHelper.sendState(state)

        handler.post {
            clickButton.text = numberOfClicks.toString()
            yourScoreBar.progress = numberOfClicks
            if (!lock && numberOfClicks >= MAX) {
                clickButton.text = "You win!"
                clickButton.isEnabled = false
                lock = true
            }
        }

    }

    var callback = object : CAppCallback {
        override fun onStatusChanged(status: Long) {
            Log.e(TAG, "createNewCAppSession onStatusChanged is: $status")
        }

        override fun onReceiveState(state: ByteArray?): Boolean {
            Log.e(TAG, "createNewCAppSession onReceiveState : $state")


            handler.post {

                state?.let {
                    if (!lock) {
                        opponentScoreBar.progress = state[0].toInt()
                    }

                    if (!lock && state[0].toInt() >= MAX) {
                        clickButton.text = "You lost!"
                        clickButton.isEnabled = false
                        lock = true
                    }
                }


            }
            return true
        }
    }
}
