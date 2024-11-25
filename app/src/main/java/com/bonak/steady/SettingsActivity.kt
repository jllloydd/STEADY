package com.bonak.steady

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.view.View
import android.widget.LinearLayout
import android.content.Intent

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.ms_md).setOnClickListener {
            val intent = Intent(this, MsMdActivity::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.sr_notif).setOnClickListener {
            val intent = Intent(this, SrNotifActivity::class.java)
            startActivity(intent)
        }

    }
}