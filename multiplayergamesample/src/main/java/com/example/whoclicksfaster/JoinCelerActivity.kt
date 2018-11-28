package com.example.whoclicksfaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.payment.KeyStoreHelper
import com.example.whoclicksfaster.CelerClientAPIHelper.joinAddr
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_join_celer.*
import network.celer.celersdk.Client
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

    lateinit var celer: Celer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_celer)


//        var Celer = Celer.Builder().with(context = applicationContext).privateKey(privateKey = "").ospPlanUrl(ospPlanUrl = "").build()
//
//        Celer.settle(OnChainTimeOut.LONG)



    }


    fun createWallet(v: View) {
        generateFilePath()

        // Get keyStroeString and passwordStr
        keyStoreString = KeyStoreHelper().getKeyStoreString(this@JoinCelerActivity)
        passwordStr = KeyStoreHelper().getPassword()

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)
        joinAddr = "0x" + keyStoreJson.address

        Log.d(TAG, "keyStoreString" + keyStoreString)
        Log.d(TAG, "joinAddr: " + joinAddr)



        val profileStr = getString(R.string.cprofile, datadir)
        celer = Celer.Builder().keyStoreString(keyStoreString).passwordStr(passwordStr).ospPlanUrl(profileStr)
                .listener(listener).build()

        showTips("createWallet Success : " + joinAddr)

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
        FaucetHelper().getTokenFromPrivateNetFaucet(context = this, faucetURL = "http://54.188.217.246:3008/donate/", walletAddress = joinAddr, faucetCallBack = object : FaucetHelper.FaucetCallBack {
            override fun onSuccess() {
                Log.d(TAG, "\n faucet success")
                showTips("getTokenFromFaucet success,wait transcation complete")
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
        intent.putExtra("joinAddr", joinAddr)
        startActivity(intent)

    }

    var listener: Celer.Listener = object : Celer.Listener {
        override fun onError(code: Int, desc: String) {

            showTips(desc)

        }

        override fun onReady(celerClient: Client) {

            showTips("Join Success")

        }

        override fun onSettle() {
        }

        override fun onNewDeposit(amount: String) {
        }

        override fun onWithdraw(amount: String) {
        }

    }

    fun createAndJoinCeler(v: View) {

        celer.createAndJoinCeler()

    }



    private fun showTips(str: String) {

        handler.post {
            tips.append("\n" + str)
        }

    }

}
