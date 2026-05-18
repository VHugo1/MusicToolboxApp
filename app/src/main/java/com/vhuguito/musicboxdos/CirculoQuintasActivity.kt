package com.vhuguito.musicboxdos

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class CirculoQuintasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circulo_quintas)

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { finish() }
    }
}