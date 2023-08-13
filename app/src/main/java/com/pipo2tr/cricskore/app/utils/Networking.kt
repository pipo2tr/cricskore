package com.pipo2tr.cricskore.app.utils

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

const val cacheSize = 5 * 1024 * 1024 // 5 MB
private const val LIVE_SCORE_URL = "https://static.cricinfo.com/rss/livescores.xml"
private const val LIVE_GAME_URL = "https://www.espncricinfo.com/matches/engine/match/%s.json"

class NetworkCallback(
    private val onResponse: (Response) -> Unit,
    private val onFailure: (IOException) -> Unit
) : Callback {
    override fun onResponse(call: Call, response: Response) {
        onResponse(response)
    }

    override fun onFailure(call: Call, e: IOException) {
        onFailure(e)
    }
}

class Networking(context: Context) {
    private var client: OkHttpClient

    init {
        Log.d("NetworkRequest", "Created")
        val cache = Cache(context.cacheDir, cacheSize.toLong())
        client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("User-Agent", getRandomChromeUserAgent())
                    .method(originalRequest.method, originalRequest.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            })
            .build()
    }

    private fun networkRequest(
        url: String,
        callback: NetworkCallback
    ) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(callback)

    }

    fun getLiveScores(callback: NetworkCallback) {
        networkRequest(LIVE_SCORE_URL, callback)
    }

    fun getMatchSummary(
        id: String,
        callback: NetworkCallback
    ) {
        networkRequest(String.format(LIVE_GAME_URL, id), callback)
    }

    private fun getRandomChromeUserAgent(): String {
        val androidVersions =
            listOf("8.0.0", "8.1.0", "9", "10", "11", "12") // Android versions starting from 8

        val randomVersion = androidVersions.random()

        return "Mozilla/5.0 (Linux; Android $randomVersion) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${(80..90).random()}.0.${(4000..5000).random()}.${(100..300).random()} Mobile Safari/537.36"
    }

}