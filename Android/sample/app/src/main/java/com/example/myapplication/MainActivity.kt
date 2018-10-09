package com.example.myapplication

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import network.celer.mobile.Client
import network.celer.mobile.Mobile
import org.json.JSONObject
import java.io.File
import com.android.volley.AuthFailureError
import com.android.volley.VolleyError
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Request.Method.POST
import com.android.volley.toolbox.StringRequest




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

        getTokenFromFaucet(joinAddr)

        // Join Celer Network
        try {
            client?.joinCeler(joinAddr, clientSideDepositAmount, serverSideDepositAmount)
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


    fun addLog(txt: String?) {
        logtext.append("\n" + txt)
    }


    fun getTokenFromFaucet(walletAddress: String) {
//        val queue = Volley.newRequestQueue(this)
//        val params = JSONObject()
//        params.put("walletAddress", walletAddress)
//        val request = JsonObjectRequest(
//                Request.Method.POST, "https://faucet.metamask.io",
//                params, Response.Listener {
//
//            logtext.append("\n getTokenSucceed: " + it.toString())
//
//
//        }, Response.ErrorListener {
//
//            logtext.append("\n getToken Error: " + it.localizedMessage)
//
//        })
//
//        queue.add(request)


//        val requestQueue = Volley.newRequestQueue(applicationContext)
//        var httpurl = "https://faucet.metamask.io"
//        val params = HashMap<String, String>()
//        params["walletAddress"] = "walletAddress"
//
//        val jsonObject = JSONObject(params)
//        val jsonRequest = object : JsonObjectRequest(Method.POST, httpurl, jsonObject,
//                Response.Listener { response -> logtext.append("\n getTokenSucceed: " + response.toString()) },
//                Response.ErrorListener { error -> logtext.append("\n getToken Error: " + error.localizedMessage) }) {
//
//            override fun getHeaders(): Map<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Accept"] = "application/json"
//                headers["Content-Type"] = "application/json; charset=UTF-8"
//                return headers
//            }
//        }
//        requestQueue.add(jsonRequest)




//        var httpurl = "https://faucet.metamask.io"
        var httpurl = "https://faucet.metamask.io"
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val postsr = object : StringRequest(Request.Method.POST, httpurl, Response.Listener { s ->
            logtext.append("\n getTokenSucceed: " + s.toString())
            Toast.makeText(this@MainActivity, "volleyPostStringMonth请求成功：$s", Toast.LENGTH_SHORT).show()
        }, Response.ErrorListener {

            error -> logtext.append("\n getToken Error: " + error.localizedMessage)

        }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //创建一个集合，放的是keyvalue的key是参数名与value是参数值
                val map = HashMap<String, String>()
                map["body"] = walletAddress
                return map
            }


        }
        //设置请求标签用于加入全局队列后，方便找到
        postsr.tag = "postsr"
        //添加到全局的请求队列

        requestQueue.add(postsr)
    }

}
