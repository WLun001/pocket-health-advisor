package com.example.lun.pocket_health_advisor

import android.content.AsyncTaskLoader
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection
import com.example.lun.pocket_health_advisor.DataClassWrapper.ChatMessage
import java.io.*

/**
 * Created by Lun on 14/01/2018.
 */
class DialogflowAsyncWorker(
        context: Context,
        private var url: String,
        private val requestMethod: Int,
        private val postData: String = "")
    : AsyncTaskLoader<ArrayList<ChatMessage>>(context) {

    companion object {
        val BOT = "bot"
        val ACCESS_TOKEN = ("47836bc8e2494eabb7ea945d1b227d29").toByteArray(Charset.defaultCharset())
        var encodedAuth = "Bearer " + Base64.encode(ACCESS_TOKEN, Base64.DEFAULT)
        const val GET = 0
        const val POST = 1
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

    override fun loadInBackground(): ArrayList<ChatMessage>? {
        var chatMessage: ArrayList<ChatMessage> = ArrayList()
        try {
            var inputStream: InputStream? = null
            val connection = httpRequestBuilder(url,requestMethod)

            Log.d("Response Code", connection.responseCode.toString() + connection.responseMessage)

            if (connection.responseCode == 200) {
                inputStream = connection.inputStream
                var jsonResponse = readInput(inputStream)
                Log.d("Json response", jsonResponse)
                chatMessage = extractFromJson(jsonResponse)

                Log.d("loadInBackground", chatMessage.toString())
            }

            if (inputStream != null) {
                inputStream.close()
            }

        } catch (e: Exception) {
//            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Log.e("loader error", e.message)
        }
        return chatMessage


    }

    private fun httpRequestBuilder(url: String, request: Int): HttpsURLConnection{
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.readTimeout = 10000
        connection.connectTimeout = 15000
        connection.setRequestProperty("Authorization", "Bearer 47836bc8e2494eabb7ea945d1b227d29")
        when(request){
             GET -> {
                 connection.requestMethod = "GET"
                 connection.connect()
            }
            POST -> {
                connection.setRequestProperty("Content-Type", "application/json" )
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connect()
                val bufferedWriter = BufferedWriter(OutputStreamWriter(connection.outputStream, "UTF-8"))
                bufferedWriter.write(postData)
                bufferedWriter.flush()
                bufferedWriter.close()
                connection.outputStream.close()
            }
        }
        return connection
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

    fun extractFromJson(json: String): ArrayList<ChatMessage> {
        var chatMessage: ArrayList<ChatMessage> = ArrayList()
        var root = JSONObject(json)
        var result = root.getJSONObject("result")
        var fulfillment = result.getJSONObject("fulfillment")
        var messages = fulfillment.getJSONArray("messages")

        for (i in 0 until messages.length()) {
            var message = messages.getJSONObject(i)
            chatMessage.add(ChatMessage(message.getString("speech"), BOT))
        }
        return chatMessage
    }
}