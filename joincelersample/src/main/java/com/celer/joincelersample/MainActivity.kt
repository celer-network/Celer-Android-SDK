package com.celer.joincelersample

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.celer.celersdk.Celer
import com.celer.celersdk.KeyStoreData
import com.example.payment.KeyStoreHelper
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import network.celer.celersdk.Client
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG = "joincelersample"
    private var keyStoreString = ""
    private var passwordStr = ""
    private var datadir = ""

    private var joinAddr = ""

    var handler: Handler = Handler()


    var profileStr = ""

    var faucetURL = "http://54.188.217.246:3008/donate/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileStr = getString(R.string.cprofile, datadir)


    }

    fun joinCeler(v: View) {
        createWallet()

        getTokenFromFaucet()

    }

    fun createWallet() {
        generateFilePath()

        // Get keyStroeString and passwordStr
        keyStoreString = KeyStoreHelper().getKeyStoreString(this)
        passwordStr = KeyStoreHelper().getPassword()

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)
        joinAddr = "0x" + keyStoreJson.address

        showTips("keyStoreString" + keyStoreString)
        showTips("joinAddr: " + joinAddr)


    }


    fun getTokenFromFaucet() {

        // Get some token from faucet
        FaucetHelper().getTokenFromPrivateNetFaucet(context = this, faucetURL = faucetURL, walletAddress = joinAddr, faucetCallBack = object : FaucetHelper.FaucetCallBack {
            override fun onSuccess() {
                showTips("getTokenFromFaucet success,wait transcation complete")
                createAndJoinCeler()
            }

            override fun onFailure() {
                showTips("getTokenFromFaucet error ")
            }

        })

    }


    fun reset(v: View) {

    }


    fun createAndJoinCeler() {


        var listener: Celer.Listener = object : Celer.Listener {

            override fun onError(code: Int, desc: String) {

                showTips("Join Celer Error: $code -- $desc")

            }

            override fun onReady(celerClient: Client) {

//                cOSClient = celerClient

                showTips("Celer Ready ")
            }

        }


        Celer.Builder().keyStoreString(keyStoreString).passwordStr(passwordStr).ospPlanUrl(profileStr)
                .listener(listener).build().createAndJoinCeler()


    }


    private fun generateFilePath() {
        val file = File(this.filesDir.path, "celer")
        if (!file.exists()) {
            file.mkdir()
        }
        datadir = file.path
    }

    private fun showTips(str: String) {

        handler.post {
            tips.append("\n" + str)
        }

    }


}
