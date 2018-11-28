package com.example.payment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import network.celer.celersdk.Celersdk
import network.celer.celersdk.Client
import java.io.File

class MainActivity : AppCompatActivity() {

    // TODO: Add your own keystore and its passwordStr here. Put your receiver addr
    private var keyStoreString = ""
    private var passwordStr = ""
    private var receiverAddr = ""

    private var datadir = ""

    private val clientSideDepositAmount = "500000000000000000" // 0.5 ETH
    private val serverSideDepositAmount = "1500000000000000000" // 1.5 ETH
    private var transferAmount: String = "30000000000000000" // 0.03 ETH

    private var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        generateFilePath()

        // Get keyStoreString and passwordStr
        keyStoreString = KeyStoreHelper().getKeyStoreString(this@MainActivity)
        passwordStr = KeyStoreHelper().getPassword()


        addLog("keyStoreString: ${keyStoreString}")
        addLog("passwordStr: ${passwordStr}")

        val profileStr = getString(R.string.cprofile, datadir)

        // Init Celer Client
        try {
            client = Celersdk.newClient(keyStoreString, passwordStr, profileStr)
        } catch(e: Exception) {
            addLog("Init Celer Client Error: ${e.localizedMessage}")
        }

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
        } catch (e: Exception) {
            addLog("send cETH Error: ${e.localizedMessage}")

        }
    }

    private fun generateFilePath() {
        val generaFile = File(this.filesDir.path, "celer")
        if (!generaFile.exists()) {
            generaFile.mkdir()
        }
        datadir = generaFile.path
    }


    fun addLog(text: String?) {
        Log.d("Celer Off-chain Payment", text)
        logtext.append("\n" + text)
    }

}
