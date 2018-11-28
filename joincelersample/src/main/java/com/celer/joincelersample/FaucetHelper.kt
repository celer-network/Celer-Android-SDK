package com.celer.joincelersample

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.UnsupportedEncodingException

class FaucetHelper {

    interface FaucetCallBack {

        fun onSuccess()

        fun onFailure()

    }

    // var faucetURL = "http://54.188.217.246:3008/donate/"
    fun getTokenFromPrivateNetFaucet(context: Context, faucetURL: String, walletAddress: String, faucetCallBack: FaucetCallBack) {

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Request.Method.GET, "$faucetURL$walletAddress",
                Response.Listener {

                    response ->
                    Log.i("VOLLEY", response)

                    if (response == "200") {
                        faucetCallBack.onSuccess()
                    } else {
                        faucetCallBack.onFailure()
                    }
                },
                Response.ErrorListener {

                    error ->
                    Log.e("VOLLEY", error.toString())

                    faucetCallBack.onFailure()
                }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var responseString = ""
                if (response != null) {
                    responseString = response.statusCode.toString()
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response))
            }
        }

        requestQueue.add(stringRequest)
    }

    fun getTokenFromFaucet(context: Context, faucetURL: String, walletAddress: String, faucetCallBack: FaucetCallBack) {

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Request.Method.POST, faucetURL,
                Response.Listener {

                    response ->
                    Log.i("VOLLEY", response)

                    if (response == "200") {
                        faucetCallBack.onSuccess()
                    } else {
                        faucetCallBack.onFailure()
                    }
                },
                Response.ErrorListener {

                    error ->
                    Log.e("VOLLEY", error.toString())


                    faucetCallBack.onFailure()
                }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray? {
                try {
                    return walletAddress?.toByteArray(charset("utf-8"))
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", walletAddress, "utf-8")
                    return null
                }

            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var responseString = ""
                if (response != null) {
                    responseString = response.statusCode.toString()
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response))
            }
        }

        requestQueue.add(stringRequest)
    }
}