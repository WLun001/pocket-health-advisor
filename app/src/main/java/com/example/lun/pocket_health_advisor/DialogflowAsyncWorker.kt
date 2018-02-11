package com.example.lun.pocket_health_advisor

import android.content.AsyncTaskLoader
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Lun on 14/01/2018.
 */
class DialogflowAsyncWorker(context: Context, private var url: String)
    : AsyncTaskLoader<ArrayList<ChatbotActivity.ChatMessage>>(context) {

    companion object {
        val BOT = "bot"
        val ACCESS_TOKEN = ("47836bc8e2494eabb7ea945d1b227d29").toByteArray(Charset.defaultCharset())
        var encodedAuth = "Bearer " + Base64.encode(ACCESS_TOKEN, Base64.DEFAULT)
    }

    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
        Log.d("onStartLoading", "forceLoad()")
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
    }

    override fun loadInBackground(): ArrayList<ChatbotActivity.ChatMessage>? {
        var chatMessage: ArrayList<ChatbotActivity.ChatMessage> = ArrayList()
        var requestUrl = URL(url)
        try {
            var inputStream: InputStream? = null

            var connection = requestUrl.openConnection() as HttpsURLConnection
            connection.readTimeout = 10000
            connection.connectTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer 47836bc8e2494eabb7ea945d1b227d29")
            connection.connect()

            Log.d("Response Code", connection.responseCode.toString() + connection.responseMessage)

            if (connection.responseCode == 200) {
                inputStream = connection.inputStream
                var jsonResponse = readInput(inputStream)
                chatMessage = extractFromJson(jsonResponse) as ArrayList<ChatbotActivity.ChatMessage>

                Log.d("loadInBackground", chatMessage.toString())
            }

            if (inputStream != null) {
                inputStream.close()
            }

        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        return chatMessage


    }

    fun readInput(inputStream: InputStream): String {
        var builder = StringBuilder()
        var reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        var json = reader.readLine()
        while (json != null) {
            builder.append(json)
            json = reader.readLine()
        }
        return builder.toString()
    }

    fun extractFromJson(json: String): ArrayList<ChatbotActivity.ChatMessage> {
        var chatMessage: ArrayList<ChatbotActivity.ChatMessage> = ArrayList()
        var root = JSONObject(json)
        var result = root.getJSONObject("result")
        var fulfillment = result.getJSONObject("fulfillment")
        var messages = fulfillment.getJSONArray("messages")
        for (i in 0 until messages.length()) {
            var message = messages.getJSONObject(i)
            chatMessage.add(ChatbotActivity.ChatMessage(message.getString("speech"), BOT))
        }
        return chatMessage
    }
}