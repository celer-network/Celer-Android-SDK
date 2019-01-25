package com.example.whoclicksfaster

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_fast_click_game.*
import network.celer.celersdk.CAppCallback
import network.celer.celersdk.Celersdk
import java.nio.charset.Charset


class FastClickGameActivity : AppCompatActivity() {
    private val TAG = "who clicks faster"

    val MAX = 100
    var myScore = 0
    var opponentScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_click_game)

        myScoreBar?.max = MAX
        opponentScoreBar?.max = MAX

        Celersdk.onLog(883)
        CelerClientAPIHelper.initSession(this, GameGroupAPIHelper.groupResponse, object : CAppCallback {
            override fun onStatusChanged(status: Long) {
                Log.d(TAG, "createNewCAppSession onStatusChanged : $status")
            }

            override fun onReceiveState(state: ByteArray?): Boolean {

                runOnUiThread {

                    var stateString = String(state!!, Charset.defaultCharset())

                    info?.text = "Received : $stateString"

                    Log.d(TAG, "onReceiveState $stateString")
                    Log.d(TAG, "CelerClientAPIHelper.myIndex : ${CelerClientAPIHelper.myIndex}")
                    Log.d(TAG, "CelerClientAPIHelper.opponentIndex : ${CelerClientAPIHelper.opponentIndex}")
                    Log.d(TAG, "Player 1 score: ${stateString.split(":")[0].toInt()}")
                    Log.d(TAG, "Player 2 score : ${stateString.split(":")[1].toInt()}")

                    if (CelerClientAPIHelper.opponentIndex == 1) {
                        opponentScore = stateString.split(":")[0].toInt()
                    } else {
                        opponentScore = stateString.split(":")[1].toInt()
                    }

                    state?.let {
                        opponentScoreBar.progress = opponentScore
                        opponentScoreText.text = "Opponent score: $opponentScore"
                        myScoreBar.progress = myScore
                        myScoreText.text = "My score: $myScore"
                    }

                    Log.d(TAG, "opponent score : $opponentScore")

                }
                return true
            }
        })
        title = "Me: " + CelerClientAPIHelper.myAddress

    }


    private fun sendState() {

        Log.d(TAG, "my score : $myScore")
        Log.d(TAG, "opponent score : $opponentScore")


        var stateString = if (CelerClientAPIHelper.myIndex == 1) {

            myScore.toString() + ":" + opponentScore.toString()

        } else {
            opponentScore.toString() + ":" + myScore.toString()
        }

        CelerClientAPIHelper.sendState(stateString.toByteArray())

        info?.text = "Sent : $stateString"

    }


    fun clickMe(v: View) {
        myScore++
        runOnUiThread {
            clickButton.text = myScore.toString()
            myScoreBar.progress = myScore
            myScoreText.text = "My score: $myScore"
            sendState()
        }

    }


}
