package com.example.whoclicksfaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.payment.KeyStoreHelper
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_join_celer.*
import java.io.File

class JoinCelerActivity : AppCompatActivity() {

    private val TAG = "JoinCelerActivity"
    private var keyStoreString = ""
    private var passwordStr = ""
    private var receiverAddr = ""
    private var datadir = ""


    private val clientSideDepositAmount = "500000000000000000" // 0.5 ETH
    private val serverSideDepositAmount = "1500000000000000000" // 1.5 ETH

    var handler: Handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_celer)
    }


    fun createWallet(v: View) {
        generateFilePath()

        // Get keyStroeString and passwordStr
        keyStoreString = KeyStoreHelper().getKeyStoreString(this@JoinCelerActivity)
        passwordStr = KeyStoreHelper().getPassword()

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)

        CelerClientAPIHelper.myAddress = "0x" + keyStoreJson.address

        Log.d(TAG, "keyStoreString$keyStoreString")
        Log.d(TAG, "myAddress: ${CelerClientAPIHelper.myAddress}")

        showTips("createWallet Success : ${CelerClientAPIHelper.myAddress}")

    }


    private fun generateFilePath() {
        val file = File(this.filesDir.path, "celer")
        if (!file.exists()) {
            file.mkdir()
        }
        datadir = file.path
    }

    fun getTokenFromFaucet(v: View) {

        // Get some token from faucet
        FaucetHelper().getTokenFromPrivateNetFaucet(context = this, faucetURL = "http://54.188.217.246:3008/donate/", walletAddress = CelerClientAPIHelper.myAddress!!, faucetCallBack = object : FaucetHelper.FaucetCallBack {
            override fun onSuccess() {
                Log.d(TAG, "\n faucet success")
                showTips("getTokenFromFaucet success,wait for transaction to complete")
            }

            override fun onFailure() {
                Log.d(TAG, "\n faucet error ")
                showTips("getTokenFromFaucet error ")
            }

        })

    }

    fun createCelerClient(v: View) {
        val profileStr = getString(R.string.cprofile, datadir)
        var result = CelerClientAPIHelper.initCelerClient(keyStoreString, passwordStr, profileStr)

        showTips(result)
    }

    fun joinCeler(v: View) {
        var result = CelerClientAPIHelper.joinCeler(clientSideDepositAmount, serverSideDepositAmount)
        showTips(result)

        if (result.contains("Success")) {
            play.visibility = View.VISIBLE
        } else {
            play.visibility = View.GONE
        }
    }

    fun play(v: View) {

        var intent = Intent(this, CreateOrJoinGroupActivity::class.java)
        intent.putExtra("keyStoreString", keyStoreString)
        intent.putExtra("passwordStr", passwordStr)
        intent.putExtra("myAddress", CelerClientAPIHelper.myAddress)
        startActivity(intent)

    }


    private fun showTips(str: String) {

        handler.post {
            tips.append("\n" + str)
        }

    }

}
