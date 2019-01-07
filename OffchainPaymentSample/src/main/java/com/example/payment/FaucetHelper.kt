package com.example.payment

import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.nio.charset.Charset

class FaucetHelper {

    enum class FaucetType { RopstenTestNetETHFromMetaMask, PrivateTestNetETH }


    interface FaucetCallBack {

        fun onSuccess(response: String)

        fun onFailure(error: String)

    }

    fun getTokenFromFaucet(faucetType: FaucetType, walletAddress: String, faucetCallBack: FaucetCallBack) {

        when (faucetType) {

            FaucetType.RopstenTestNetETHFromMetaMask ->
                getTokenFromMetaMaskFaucet(walletAddress, faucetCallBack)
            FaucetType.PrivateTestNetETH ->
                getTokenFromPrivateTestNetFaucet(walletAddress, faucetCallBack)
        }

    }


    private fun getTokenFromPrivateTestNetFaucet(walletAddress: String, faucetCallBack: FaucetCallBack) {

        "https://osp1-test-priv.celer.app/donate/$walletAddress"
                .httpGet()
                .responseString { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e("Private TESTNET Faucet", "Private TESTNET Faucet Failed. " +
                                    "Please use the standard Ropsten cNode profile or contact developer@celer.network")
                            val ex = result.getException()
                            faucetCallBack.onFailure(ex.localizedMessage)
                        }
                        is Result.Success -> {
                            val response = result.get()
                            faucetCallBack.onSuccess(response)
                        }
                    }
                }
    }


    private fun getTokenFromMetaMaskFaucet(walletAddress: String, faucetCallBack: FaucetCallBack) {

        "https://faucet.metamask.io"
                .httpPost()
                .body(walletAddress, Charset.defaultCharset())
                .responseString { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e("MetaMaskFaucet", "MetaMask Faucet Failed. " +
                                    "You can either try again, use another Ropsten ETH faucet, " +
                                    "\nor use an wallet that already has some Rospten ETH to transfer some on-chain balance to this account: $walletAddress")
                            val ex = result.getException()
                            faucetCallBack.onFailure(ex.localizedMessage)
                        }
                        is Result.Success -> {
                            val response = result.get()
                            faucetCallBack.onSuccess(response)
                        }
                    }
                }

    }

}