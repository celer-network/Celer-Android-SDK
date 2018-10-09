package com.example.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import network.celer.mobile.Client
import network.celer.mobile.Mobile
import java.io.File


class MainActivity : AppCompatActivity() {

    private var keyStoreString = ""
    private var passwordStr = ""
    private var receiverAddr = ""

    private var datadir = ""

    private val clientSideDepositAmount = "500000000000000000" // 0.5 cETH
    private val serverSideDepositAmount = "1500000000000000000" // 1.5 cETH
    private var transferAmount: String = "30000000000000000" // 0.03 ETH

    private var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        generateFilePath()

        // Get keyStroeString and passwordStr
        keyStoreString = KeyStoreHelper().getKeyStoreString(this@MainActivity)
        passwordStr = KeyStoreHelper().getPassword()

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)
        var joinAddr = "0x" + keyStoreJson.address

        addLog("keyStoreString: ${keyStoreString}")
        Log.d("MainActivity", keyStoreString)
        addLog("passwordStr: ${passwordStr}")

        val profileStr = getString(R.string.cprofile, datadir)

        // Init Celer Client
        try {
            client = Mobile.newClient(keyStoreString, passwordStr, profileStr)
        } catch(e: Exception) {
            addLog("Init Celer Client Error: ${e.localizedMessage}")
        }


        // Deposit some token from faucet
        FaucetHelper().getTokenFromFaucet(this, walletAddress = joinAddr, faucetCallBack = object : FaucetHelper.FaucetCallBack {
            override fun onSuccess() {

                logtext.append("\n getTokenSucceed ")

                // Join Celer Network
                try {
                    client?.joinCeler("0x0", clientSideDepositAmount, serverSideDepositAmount)
                    addLog("Balance: ${client?.getBalance(1)?.available}")
                } catch (e: Exception) {
                    addLog("Join Celer Network Error: ${e.localizedMessage}")

                }

                // check if an address has joined Celer Network
                try {
                    receiverAddr = "0x2718aaa01fc6fa27dd4d6d06cc569c4a0f34d399"
                    val hasJoin = client?.hasJoinedCeler(receiverAddr)
                    addLog("hasJoin: $hasJoin")
                } catch (e: Exception) {
                    addLog("check Error: ${e.localizedMessage}")
                }

                // send cETH to an address
                try {
                    client?.sendPay(receiverAddr, transferAmount)
                    addLog("sendPay: done")
                } catch (e: Exception) {
                    addLog("send cETH Error: ${e.localizedMessage}")

                }

            }

            override fun onFailure() {

                logtext.append("\n getToken Error ")

            }

        })




    }

    private fun generateFilePath() {
        val generaFile = File(this.filesDir.path, "celer")
        if (!generaFile.exists()) {
            generaFile.mkdir()
        }
        datadir = generaFile.path
    }


    fun addLog(txt: String?) {
        logtext.append("\n" + txt)
    }




}
