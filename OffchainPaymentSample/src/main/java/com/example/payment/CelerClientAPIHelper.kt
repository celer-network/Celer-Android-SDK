package com.example.payment

import android.content.Context
import android.util.Log
import network.celer.celersdk.*

object CelerClientAPIHelper {

    private val TAG = "CelerClientAPIHelper"
    private var client: Client? = null

    /*Please use Ropsten Testnet as your default cNode profile during development.
    You can view all on-chain balances and on-chain transactions on https://ropsten.etherscan.io*/

    fun getRopstenTestNetProfile(context: Context): String {
        return context.getString(R.string.cprofile_ropsten, KeyStoreHelper.generateFilePath(context))
    }

    /*Private Testnet cNode Profile (Optional):
    The private Testnet profile is for simple test purpose only.
    When you have difficulties getting Ropsten testnet tokens or when Ropsten network congestion happens,
    you can temporarily use this profile to quickly test some basic off-chain Celer APIs.
    Not all APIs can be tested with this profile.
    We currently do not support viewing on-chain balances and on-chain transactions on private Testnet.*/

    fun getPrivateTestNetProfile(context: Context): String {
        return context.getString(R.string.cprofile_private_testnet, KeyStoreHelper.generateFilePath(context))
    }

    fun initCelerClient(keyStoreString: String, passwordStr: String, profile: String): String {
        // Init Celer Client
        try {
            client = Celersdk.newClient(keyStoreString, passwordStr, profile)
            Log.d(TAG, "Celer client created")
            return "Celer client created"
        } catch (e: Exception) {
            Log.d(TAG, e.localizedMessage)
            return e.localizedMessage
        }
    }

    fun joinCeler(clientSideDepositAmount: String, serverSideDepositAmount: String): String {
        // Join Celer Network
        try {
            client?.joinCeler("0x0", clientSideDepositAmount, serverSideDepositAmount)
            return "joinCeler: successful"
        } catch (e: Exception) {
            Log.d(TAG, "Join Celer Network Error: ${e.localizedMessage}")
            return "Join Celer Network Error: ${e.localizedMessage}"
        }

    }

    fun checkBalance(): String {
        // check has joined Celer
        try {
            val balance = client?.getBalance(1L)?.available
            Log.d(TAG, "current Balance: $balance")
            return "$balance wei"
        } catch (e: Exception) {
            Log.d(TAG, "Check Balance Error: ${e.localizedMessage}")
            return "Check Balance Error: ${e.localizedMessage}"
        }

    }

    fun sendPayment(receiverAddress: String, transferAmount: String): String {
        try {
            client?.sendPay(receiverAddress.replace("0x", "").toLowerCase(), transferAmount)
            return "Send payment: successful"
        } catch (e: Exception) {
            Log.d(TAG, "send PaymentError: ${e.localizedMessage}")
            return "Send payment Error: ${e.localizedMessage}"
        }

    }


}