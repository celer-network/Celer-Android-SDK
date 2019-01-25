package com.example.whoclicksfaster

import android.util.Log
import network.celer.appsdk.*

object GameGroupAPIHelper {


    private const val TAG = "who clicks faster"
    var groupClient: GroupClient? = null
    var groupResponse: GroupResp? = null

    fun createNewGroupClient(keyStoreString: String, passwordStr: String, callback: GroupCallback): String {
        return try {
            groupClient = Appsdk.newGroupClient("group-test-priv.celer.app:10001", keyStoreString, passwordStr, callback)
            Log.d(TAG, "Connected to Group Server")
            "Connected to Group Server Success"
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            e.toString()
        }
    }


    fun createGame(myAddress: String): String {
        leave(myAddress)
        var group = Group()
        group.myId = myAddress
        group.size = 2
        group.stake = "1000000000000000"
        Log.d(TAG, "Create: " + group.toString())
        try {
            groupClient?.createPrivate(group)
            return "Success"
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            return e.toString()
        }
    }


    fun joinGame(myAddress: String, code: Long, stake:String) {
        leave(myAddress)
        var group = Group()
        group.myId = myAddress
        group.code = code
        group.stake = stake

        try {
            groupClient?.joinPrivate(group)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }


    fun leave(myAddress: String) {
        groupClient?.let {
            Log.d(TAG, "leave previous group")
            var group = Group()
            group.myId = myAddress
            it.leave(group)
        }
    }


}