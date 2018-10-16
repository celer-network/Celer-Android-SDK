package com.example.whoclicksfaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_or_join_group.*
import network.celer.mobile.GroupCallback
import network.celer.mobile.GroupResp

class CreateOrJoinGroupActivity : AppCompatActivity(), GroupCallback {

    private val TAG = "CreateOrJoinGroup"

    private var keyStoreString = ""
    private var passwordStr = ""
    private var joinAddr = ""

    var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_join_group)


        keyStoreString = intent.getStringExtra("keyStoreString")
        passwordStr = intent.getStringExtra("passwordStr")
        joinAddr = intent.getStringExtra("joinAddr")
    }


    fun createGroupClient(v: View) {

        var result = GameGroupAPIHelper.createNewGroupClient(keyStoreString, passwordStr, this)

        showTips("createNewGroupClient : $result")

        if (result.contains("Success")) {

            btnCreateGame.isEnabled = true
            btnJoinGame.isEnabled = true
            btnCreatGroupClient.visibility = View.INVISIBLE

        } else {
            Toast.makeText(applicationContext, "CreateGroupClient failure. Try again later.", Toast.LENGTH_LONG).show()
        }



    }

    fun createGame(v: View) {

        var result = GameGroupAPIHelper.createGame(joinAddr)

        showTips("createGame : $result")

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

                GameGroupAPIHelper.gresp = gresp


                var intent = Intent(this, FastClickGameActivity::class.java)
                startActivity(intent)
            }
        }

    }



    fun joinGame(v: View) {
        var code = etJoinCode.text.toString().toLong()
        var result = GameGroupAPIHelper.joinGame(joinAddr, code)

        showTips("joinGame : $result")

    }


    private fun showTips(str: String) {

        handler.post {
            tips.append("\n" + str)
        }

    }
}
