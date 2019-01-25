package com.example.whoclicksfaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_or_join_group.*
import network.celer.appsdk.GroupCallback
import network.celer.appsdk.GroupResp

class CreateOrJoinGroupActivity : AppCompatActivity(), GroupCallback {

    private val TAG = "CreateOrJoinGroup"

    private var keyStoreString = ""
    private var passwordStr = ""

    var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_join_group)


        keyStoreString = intent.getStringExtra("keyStoreString")
        passwordStr = intent.getStringExtra("passwordStr")
    }


    fun createGame(v: View) {

        var result = GameGroupAPIHelper.createNewGroupClient(keyStoreString, passwordStr, this)

        showTips("createNewGroupClient : $result")

        if (GameGroupAPIHelper.groupClient == null || !result.contains("Success")) {
            Toast.makeText(applicationContext, "GameGroupAPIHelper.createNewGroupClient failure. Try again later.", Toast.LENGTH_LONG).show()
        } else {
            var result = GameGroupAPIHelper.createGame(CelerClientAPIHelper.myAddress!!)

            showTips("createGame : $result")
        }


    }

    override fun onRecvGroup(gresp: GroupResp?, err: String?) {
        Log.e(TAG, "OnRecvGroup--------------------:")
        Log.e(TAG, gresp?.toString())
        Log.e(TAG, err)
        gresp?.let {

            handler.post {
                var code = it.g.code.toString()
                tvCode.text = "JoinCode: $code"
            }


            if (it.g.users.split(",").size == 2) {
                Log.d(TAG, "Matched with a player!")
                showTips("Matched with a player!")

                GameGroupAPIHelper.groupResponse = gresp

                var intent = Intent(this, FastClickGameActivity::class.java)
                startActivity(intent)
            }
        }

    }



    fun joinGame(v: View) {
        var result = GameGroupAPIHelper.createNewGroupClient(keyStoreString, passwordStr, this)

        var code = etJoinCode.text.toString().toLong()
        var stake = "10"
        GameGroupAPIHelper.joinGame(CelerClientAPIHelper.myAddress!!, code, stake)
        showTips("joining game")
    }


    private fun showTips(str: String) {

        handler.post {
            tips.append("\n" + str)
        }

    }
}
