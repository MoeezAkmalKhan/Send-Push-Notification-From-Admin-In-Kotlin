package com.moeez.pushnotification.admin

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moeez.pushnotification.databinding.ActivityNotificationBinding
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NotificationActivity : AppCompatActivity() {
    private var binding: ActivityNotificationBinding? = null
    private val mContext: Context = this
    private val channelId: String = "udp_channel"
    private val topic: String = "channelName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSend?.setOnClickListener {
            val title = binding?.etTitle?.text.toString()
            val message = binding?.etMessage?.text.toString()

            if (title.isNotEmpty() && message.isNotEmpty()) {
                pushNotification(topic, title, message).execute()

            } else {
                Toast.makeText(this, "Title or Message should not be empty.", Toast.LENGTH_SHORT).show()
            }
            binding?.etTitle?.text?.clear()
            binding?.etMessage?.text?.clear()
        }

    }

    private inner class pushNotification(private val topicName: String, private val title: String,
                                         private val body: String) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val url = URL("https://fcm.googleapis.com/fcm/send")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Authorization",
                    "key= enter your firebase cloud messaging server id"
                )
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonParam = JSONObject().apply {
                    put("to", "/topics/$topicName")
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                        put("sound", "default")
                    })
                }

                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(jsonParam.toString())
                outputStreamWriter.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    return response
                } else {
                    val error = connection.errorStream.bufferedReader().use { it.readText() }
                    return error
                }
            } finally {
                connection.disconnect()
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                // Handle the result here
                Toast.makeText(mContext, "Push Sent Successfully.", Toast.LENGTH_SHORT).show()
                println("fcm.google: $result")
                Log.e("fcm.google:", "onPostExecute: $result", )
            }
        }

    }

}