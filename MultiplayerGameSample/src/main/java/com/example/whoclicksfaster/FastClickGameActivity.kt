package com.example.whoclicksfaster

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_fast_click_game.*
import network.celer.celersdk.CAppCallback
import org.spongycastle.util.encoders.Hex
import android.os.CountDownTimer


class FastClickGameActivity : AppCompatActivity() {
    private val TAG = "who clicks faster"

    val MAX = 50

    var myScore = 0

    //var myFinalScore = 0

    var opponentScore = 0

    //var opponentFinalScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_click_game)

        myScoreBar?.max = MAX
        opponentScoreBar?.max = MAX

        CelerClientAPIHelper.initSession(this, GameGroupAPIHelper.groupResponse, callback)


    }


    private fun sendState() {

        var state = ByteArray(2)

        if (CelerClientAPIHelper.myIndex == 1) {
            state[0] = myScore.toByte()
            state[1] = opponentScore.toByte()

            //state[2] = myFinalScore.toByte()
            //state[3] = opponentFinalScore.toByte()

        } else {
            state[0] = opponentScore.toByte()
            state[1] = myScore.toByte()

            //state[2] = opponentFinalScore.toByte()
            //state[3] = myFinalScore.toByte()
        }

        CelerClientAPIHelper.sendState(state)
    }


    fun clickMe(v: View) {
        myScore++

        sendState()

        /*handler.post {
            clickButton.text = myScore.toString()
            myScoreBar.progress = myScore
            myScoreText.text = "My score: $myScore"
        }*/

    }

    private var callback = object : CAppCallback {
        override fun onStatusChanged(status: Long) {
            Log.d(TAG, "createNewCAppSession onStatusChanged : $status")
        }

        override fun onReceiveState(state: ByteArray?): Boolean {
            Log.d(TAG, "createNewCAppSession onReceiveState : ${Hex.toHexString(state)}")

            Log.d(TAG, "CelerClientAPIHelper.myIndex : ${CelerClientAPIHelper.myIndex}")
            Log.d(TAG, "CelerClientAPIHelper.opponentIndex : ${CelerClientAPIHelper.opponentIndex}")
            Log.d(TAG, "Player 1 score: ${state!![0].toInt()}")
            Log.d(TAG, "Player 2 score : ${state!![1].toInt()}")

            if (CelerClientAPIHelper.opponentIndex == 1) {
                myScore = state!![1].toInt()
                opponentScore = state!![0].toInt()
            } else {
                myScore = state!![0].toInt()
                opponentScore = state!![1].toInt()
            }


                state?.let {
                    opponentScoreBar.progress = opponentScore
                    opponentScoreText.text = "Opponent score: $opponentScore"
                    myScoreBar.progress = myScore
                    myScoreText.text = "My score: $myScore"
                }

                Log.d(TAG, "opponent score : $opponentScore")

            return true
        }
    }
}
