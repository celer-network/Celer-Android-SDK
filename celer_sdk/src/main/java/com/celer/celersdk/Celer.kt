package com.celer.celersdk

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import network.celer.celersdk.Celersdk
import network.celer.celersdk.Client
import java.math.BigInteger
import java.util.*

class Celer private constructor(builder: Builder) {
    private val TAG = "Celer"

    private val CLIENT_CREATE_ERROR: Int = 0
    private val JOIN_CELER_NO_OFFCHAIN_BALANCE_ERROR: Int = 1
    private val JOIN_CELER_ERROR: Int = 2
    private val CLIENT_CREATE_AND_JOIN_TIMEOUT_ERROR: Int = 3


    private var context: Context? = null

    var celerClient: Client? = null

    private var keyStoreString: String? = null
    private var passwordStr: String? = null

    private var ospPlanUrl: String? = null

    private var clientSideDepositAmount = "500000000000000000" // 0.5 ETH
    private var serverSideDepositAmount = "1500000000000000000" // 1.5 ETH


    private var timeout: Long? = 1000 * 60 * 3

    private var listener: Listener? = null


    private var joinAddr: String? = null

    private var channelId: String? = null

    private var countDownTimer: CountDownTimer? = null
    private val timeCountInMilliSeconds: Long = 1000 * 60 * 1 // 6 minutes

    var timer = Timer()
    lateinit var task: TimerTask


    fun deposit(amount: String, timeout: Long) {}

    fun withdraw(amount: String, timeout: Long) {}

    fun settle(timeout: Long) {}

    interface Listener {

        fun onError(code: Int, desc: String)

        fun onReady(celerClient: Client)

//        fun onSettle()
//
//        fun onNewDeposit(amount: String)
//
//        fun onWithdraw(amount: String)

    }


    init {
        context = builder.context

        keyStoreString = builder.keyStoreString
        passwordStr = builder.passwordStr

        ospPlanUrl = builder.ospPlanUrl
        timeout = builder.timeout

        listener = builder.listener

        clientSideDepositAmount = builder.clientSideDepositAmount
        serverSideDepositAmount = builder.serverSideDepositAmount


    }


    fun createAndJoinCeler() {
        launch {
            async {

                try {
                    task = object : TimerTask() {
                        override fun run() {
                            listener!!.onError(CLIENT_CREATE_AND_JOIN_TIMEOUT_ERROR, "Celer client created Error: Time Out!!!")
                        }
                    }
                    timer.schedule(task, timeout!!)


                    if (celerClient == null) {
                        initCelerClient(keyStoreString!!, passwordStr!!, ospPlanUrl!!)
                    }


                    if (celerClient != null) {
                        if (hasJoinedCeler() == BigInteger.ZERO) {
                            joinCeler(clientSideDepositAmount = clientSideDepositAmount, serverSideDepositAmount = serverSideDepositAmount)
                        } else {
                            listener!!.onReady(celerClient = celerClient!!)
                            task.cancel()
                        }
                    }
                } catch (e: Exception) {
                    task.cancel()
                    listener!!.onError(CLIENT_CREATE_AND_JOIN_TIMEOUT_ERROR, "Celer client created Error: ${e.localizedMessage}")

                }


            }
        }


    }


    private fun hasJoinedCeler(): BigInteger {
        var sendCapacity = celerClient!!.hasJoinedCeler(joinAddr)
        if (sendCapacity.isNullOrEmpty()) {
            sendCapacity = "0"
        }
        Log.d(TAG, "hasJoinedCeler: $sendCapacity")
        return sendCapacity.toBigInteger()
    }


    private fun initCelerClient(keyStoreString: String, passwordStr: String, profileStr: String) {
        // Init Celer Client


        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)

        Log.d(TAG, "address in keyStoreJson: ${keyStoreJson.address}")

        joinAddr = "0x" + keyStoreJson.address


        try {
            celerClient = Celersdk.newClient(keyStoreString, passwordStr, profileStr)

            Log.d(TAG, "Celer client created")
        } catch (e: Exception) {
            Log.d(TAG, "Celer client created Error: ${e.localizedMessage}")
            listener!!.onError(CLIENT_CREATE_ERROR, "Celer client created Error: ${e.localizedMessage}")
            task.cancel()
        }


    }


    private fun joinCeler(clientSideDepositAmount: String, serverSideDepositAmount: String) {
        // Join Celer Network


        try {
            channelId = celerClient!!.joinCeler("0x0", clientSideDepositAmount, serverSideDepositAmount)

            var offchainBalance = celerClient!!.getBalance(1L)?.available ?: "0"
            Log.d(TAG, "Balance: ${celerClient!!.getBalance(1L)?.available}")
            if (hasJoinedCeler() > BigInteger.ZERO) {
                listener!!.onReady(celerClient = celerClient!!)
                task.cancel()
            } else {
                task.cancel()
                listener!!.onError(JOIN_CELER_NO_OFFCHAIN_BALANCE_ERROR, "no offchainBalance token")
            }

        } catch (e: Exception) {
            task.cancel()
            Log.d(TAG, "Join Celer Network Error: ${e.localizedMessage}")
            listener!!.onError(JOIN_CELER_ERROR, "Join Celer Network Error: ${e.localizedMessage}")

        }


    }


    class Builder {
        internal var context: Context? = null

        internal var keyStoreString: String? = null
        internal var passwordStr: String? = null

        internal var ospPlanUrl: String? = null
        internal var timeout: Long? = 1000 * 60 * 3

        internal var listener: Listener? = null

        var clientSideDepositAmount = "500000000000000000" // 0.5 ETH
        var serverSideDepositAmount = "1500000000000000000" // 1.5 ETH

        fun with(context: Context): Builder {
            this.context = context
            return this
        }

        fun keyStoreString(keyStoreString: String): Builder {
            this.keyStoreString = keyStoreString
            return this
        }

        fun passwordStr(passwordStr: String): Builder {
            this.passwordStr = passwordStr
            return this
        }

        fun ospPlanUrl(ospPlanUrl: String): Builder {
            this.ospPlanUrl = ospPlanUrl
            return this
        }

        fun timeout(timeout: Long): Builder {
            this.timeout = timeout
            return this
        }

        fun listener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        fun clientSideDepositAmount(clientSideDepositAmount: String): Builder {
            this.clientSideDepositAmount = clientSideDepositAmount
            return this
        }

        fun serverSideDepositAmount(serverSideDepositAmount: String): Builder {
            this.serverSideDepositAmount = serverSideDepositAmount
            return this
        }

        fun build(): Celer {
            return Celer(this)
        }
    }
}