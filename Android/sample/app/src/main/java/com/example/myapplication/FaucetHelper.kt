package com.example.myapplication

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

    fun getTokenFromFaucet(context: Context, walletAddress: String, faucetCallBack: FaucetCallBack) {

        val requestQueue = Volley.newRequestQueue(context)
        val URL = "https://faucet.metamask.io"
        val requestBody = walletAddress

        val stringRequest = object : StringRequest(Request.Method.POST, URL,
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
                    return requestBody?.toByteArray(charset("utf-8"))
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8")
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