package com.vhuguito.musicboxdos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtEstadoCuenta: TextView
    private lateinit var txtSoporte: TextView
    private lateinit var txtPrivacidad: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        btnBack = findViewById(R.id.btn_back)
        txtEstadoCuenta = findViewById(R.id.txt_estado_cuenta)
        txtSoporte = findViewById(R.id.txt_soporte)
        txtPrivacidad = findViewById(R.id.txt_privacidad)

        btnBack.setOnClickListener { finish() }

        // Determinar estado de la cuenta
        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val sinAnuncios = prefs.getBoolean("sin_anuncios", false)
        val esPremium = prefs.getBoolean("is_premium", false)

        val estado = when {
            esPremium -> "Premium (accesos completos)"
            sinAnuncios -> "Sin Anuncios"
            else -> "Gratuita"
        }
        txtEstadoCuenta.text = estado

        // Soporte: abrir correo
        txtSoporte.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:soporte@vhuguitosoftware.com")
                putExtra(Intent.EXTRA_SUBJECT, "Soporte Music Toolbox")
            }
            startActivity(intent)
        }

        // Política de Privacidad: abrir URL (cambiar por tu URL real)
        txtPrivacidad.setOnClickListener {
            val url = "https://pastebin.com/rwFQqSWe"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}