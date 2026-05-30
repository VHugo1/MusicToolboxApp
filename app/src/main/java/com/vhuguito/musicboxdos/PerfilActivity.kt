package com.vhuguito.musicboxdos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vhuguito.musicboxdos.BuildConfig

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtEstadoCuenta: TextView
    private lateinit var txtSoporte: TextView
    private lateinit var txtPrivacidad: TextView
    private lateinit var txtVersion: TextView
    private lateinit var imgPromocional: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        btnBack = findViewById(R.id.btn_back)
        txtEstadoCuenta = findViewById(R.id.txt_estado_cuenta)
        txtSoporte = findViewById(R.id.txt_soporte)
        txtPrivacidad = findViewById(R.id.txt_privacidad)
        txtVersion = findViewById(R.id.txt_version)
        imgPromocional = findViewById(R.id.imgPromocional)

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

        // Mostrar u ocultar imagen promocional según Premium
        if (esPremium) {
            imgPromocional.visibility = View.VISIBLE
        } else {
            imgPromocional.visibility = View.GONE
        }

        // Mostrar número de versión
        txtVersion.text = "Music Toolbox ${BuildConfig.VERSION_NAME}"

        // Soporte: abrir correo
        txtSoporte.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:vhrm007@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Soporte Music Toolbox")
            }
            startActivity(intent)
        }

        // Política de Privacidad: abrir URL
        txtPrivacidad.setOnClickListener {
            val url = "https://sites.google.com/view/vhuguitosoftware/pol%C3%ADtica-de-privacidad"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}