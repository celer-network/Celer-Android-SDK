package com.example.whoclicksfaster

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import network.celer.appsdk.GroupResp
import network.celer.celersdk.*
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.io.InputStream
import java.util.*

object CelerClientAPIHelper {

    private val TAG = "who clicks faster"
    private var client: Client? = null

    var opponentIndex = -1
    var myIndex = -1
    var myAddress: String? = null
    private var opponentAddress: String? = null

    var sessionId: String? = null

    val cApp = CApp()


    fun initCelerClient(keyStoreString: String, passwordStr: String, profileStr: String): String {
        // Init Celer Client
        try {
            client = Celersdk.newClient(keyStoreString, passwordStr, profileStr)
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
            Log.d(TAG, "Balance: ${client?.getBalance(1L)?.available}")
            return "joinCeler Success ,Balance:${client?.getBalance(1L)?.available}"
        } catch (e: Exception) {
            Log.d(TAG, "Join Celer Network Error: ${e.localizedMessage}")
            return "Join Celer Network Error: ${e.localizedMessage}"

        }

    }


    fun initSession(context: Context, gresp: GroupResp?, callback: CAppCallback) {
        gresp?.let {
            it?.g.let {

                val playerAddresses = it.users.split(",")

                if (playerAddresses.size == 2) {

                    if (playerAddresses[0].toLowerCase() == myAddress!!.toLowerCase()) {
                        opponentAddress = playerAddresses[1]
                        myIndex = 1
                        opponentIndex = 2
                    } else {
                        opponentAddress = playerAddresses[0]
                        opponentIndex = 1
                        myIndex = 2
                    }

                    var gomokuABI: String? = null
                    var gomokuBIN: String? = null

                    //read ABI
                    try {
                        val inputStream: InputStream = context.assets.open("Gomoku.abi")
                        gomokuABI = inputStream.bufferedReader().use { it.readText() }
//                        Log.e(TAG, "createNewCAppSession gomokuABI is: $gomokuABI")
                    } catch (e: Exception) {
                        Log.d(TAG, "createNewCAppSession gomokuABI reading exception: ${e.message}")
                    }

                    //read bin
                    try {
                        val inputStream: InputStream = context.assets.open("Gomoku.bin")
                        gomokuBIN = inputStream.bufferedReader().use { it.readText() }
//                        Log.e(TAG, "createNewCAppSession gomokuBIN is: $gomokuBIN")
                    } catch (e: Exception) {
                        Log.d(TAG, "createNewCAppSession gomokuBIN reading exception:  ${e.message}")
                    }


//                    cApp.contractAbi = gomokuABI
//                    cApp.contractBin = gomokuBIN

                    cApp.callback = callback

                    val constructor = FunctionEncoder.encodeConstructor(Arrays.asList(
                            Address(playerAddresses[0]),
                            Address(playerAddresses[1]),
                            Uint256(3),
                            Uint256(3),
                            Uint8(5),
                            Uint8(3)))

                    try {
                        sessionId = client?.newCAppSession(cApp, "", gresp.round.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "newCAppSession Error: ${e.localizedMessage}")

                    }

                    Log.e(TAG, "myAddress : $myAddress")
                    Log.e(TAG, "opponentAddress : $opponentAddress")
                    Log.e(TAG, "sessionId : $sessionId")
                    Log.e(TAG, "groupResponse.round.id : ${gresp.round.id}")


//                    val delay = if (myIndex == 1) 2000L else 0L
//                    var stake = it.stake.split(".")[0]
//
//                    sendPayWithConditions(stake, opponentIndex)

                }


            }

        }


    }

    fun sendPaymentWithConditions(amount: String, indexOpponent: Int) {
        val booleanCondition = BooleanCondition()
        booleanCondition.timeout = 500
        booleanCondition.sessionID = sessionId
        val argsForQueryResult = byteArrayOf(1)
        argsForQueryResult[0] = indexOpponent.toByte()
        booleanCondition.argsForQueryResult = argsForQueryResult

        Log.e(TAG, "sendPayWithCondtions: argsForQueryResult[0]: ${argsForQueryResult[0]}")

        try {
            client?.sendPayWithConditions(opponentAddress, amount, booleanCondition)
            Log.e(TAG, "sendPay: sent")
        } catch (e: Exception) {
            Log.e(TAG, "sendPayWithConditions Error: ${e.localizedMessage}")

        }


    }

    fun sendState(state: ByteArray) {
        Log.e(TAG, "sessionId : $sessionId")
        Log.e(TAG, "myAddress : $myAddress")
        Log.e(TAG, "opponentAddress : $opponentAddress")
        client?.sendCAppState(sessionId, opponentAddress, state)

    }
}