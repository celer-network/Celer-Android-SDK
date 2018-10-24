package com.example.whoclicksfaster

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import network.celer.mobile.Client
import network.celer.mobile.Mobile
import java.math.BigInteger

class Celer private constructor(builder: Builder) {
    private val TAG = "Celer"

    internal var context: Context? = null

    internal var celerClient: Client? = null

    internal var keyStoreString: String? = null
    internal var passwordStr: String? = null

    internal var ospPlanUrl: String? = null
    internal var timeout: Long? = null

    internal var listener: Listener? = null


    lateinit var joinAddr: String


    fun deposit(amount: String, timeout: Long) {}

    fun withdraw(amount: String, timeout: Long) {}

    fun settle(timeout: Long) {}

    interface Listener {

        fun onError(code: Int, desc: String)

        fun onReady(celerClient: Client)

        fun onSettle()

        fun onNewDeposit(amount: String)

        fun onWithdraw(amount: String)

    }


    init {
        context = builder.context

        keyStoreString = builder.keyStoreString
        passwordStr = builder.passwordStr

        ospPlanUrl = builder.ospPlanUrl
        timeout = builder.timeout

        listener = builder.listener


        initCelerClient(keyStoreString!!,passwordStr!!,ospPlanUrl!!)

    }


    fun initCelerClient(keyStoreString: String, passwordStr: String, profileStr: String) {
        // Init Celer Client

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)

        Log.d(TAG, "address in keyStoreJson: ${keyStoreJson.address}")

        joinAddr = "0x" + keyStoreJson.address


        try {
            celerClient = Mobile.newClient(keyStoreString, passwordStr, profileStr)
            Log.d(TAG, "Celer client created")
        } catch (e: Exception) {
            Log.d(TAG, "Celer client created Error: ${e.localizedMessage}")
            listener!!.onError(0, "Celer client created Error: ${e.localizedMessage}")
        }
    }

    fun joinCeler(clientSideDepositAmount: String, serverSideDepositAmount: String) {
        // Join Celer Network
        try {
            celerClient?.joinCeler("0x0", clientSideDepositAmount, serverSideDepositAmount)

            var offchainBalance = celerClient?.getBalance(1L)?.available ?: "0"
            Log.d(TAG, "Balance: ${celerClient?.getBalance(1L)?.available}")
            if (offchainBalance.toBigInteger() > BigInteger.ZERO) {
                listener!!.onReady(celerClient = celerClient!!)
            } else {
                listener!!.onError(0, "no enough token")
            }

        } catch (e: Exception) {
            Log.d(TAG, "Join Celer Network Error: ${e.localizedMessage}")
            listener!!.onError(0, "Join Celer Network Error: ${e.localizedMessage}")

        }

    }


    class Builder {
        internal var context: Context? = null

        internal var keyStoreString: String? = null
        internal var passwordStr: String? = null

        internal var ospPlanUrl: String? = null
        internal var timeout: Long? = null

        internal var listener: Listener? = null

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

        fun build(): Celer {
            return Celer(this)
        }
    }
}