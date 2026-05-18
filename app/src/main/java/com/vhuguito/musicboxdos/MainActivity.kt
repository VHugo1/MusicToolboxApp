package com.vhuguito.musicboxdos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        val btnAfinador = findViewById<Button>(R.id.btn_afinador)
        val btnMetronomo = findViewById<Button>(R.id.btn_metronomo)
        val btnAcordes = findViewById<Button>(R.id.btn_acordes)
        val btnTablaturas = findViewById<Button>(R.id.btn_tablaturas)
        val btnPerfil = findViewById<ImageButton>(R.id.btn_perfil)
        val btnStar = findViewById<ImageButton>(R.id.btn_star)
        val btnDiamante = findViewById<ImageButton>(R.id.btn_diamante)
        val btnStarText = findViewById<TextView>(R.id.btn_star_text)
        val btnDiamanteText = findViewById<TextView>(R.id.btn_diamante_text)
        val adView = findViewById<AdView>(R.id.adView)

        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val sinAnuncios = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("sin_anuncios", false)
        val isPremium = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("is_premium", false)

        // Modo Premium o Sin anuncios: ocultar banner y botones de pago
        if (sinAnuncios || isPremium) {
            adView.visibility = View.GONE
            btnStar.visibility = View.GONE
            btnDiamante.visibility = View.GONE
            btnStarText.visibility = View.GONE
            btnDiamanteText.visibility = View.GONE

            // Aumentar altura de los botones principales para ocupar espacio
            val params = btnAfinador.layoutParams
            params.height = 180
            btnAfinador.layoutParams = params
            btnMetronomo.layoutParams = params
            btnAcordes.layoutParams = params
            btnTablaturas.layoutParams = params
        } else {
            adView.visibility = View.VISIBLE
            btnStar.visibility = View.VISIBLE
            btnDiamante.visibility = View.VISIBLE
            btnStarText.visibility = View.VISIBLE
            btnDiamanteText.visibility = View.VISIBLE
        }
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Listeners
        btnAfinador.setOnClickListener {
            startActivity(Intent(this, TunerActivity::class.java))
        }

        btnMetronomo.setOnClickListener {
            val intent = Intent(this, MetronomeActivity::class.java)
            startActivity(intent)
        }

        btnAcordes.setOnClickListener {
            if (isPremium) {
                startActivity(Intent(this, AcordesActivity::class.java))
            } else {
                Toast.makeText(this, "❌ Disponible solo en versión Premium (\$249 MXN)", Toast.LENGTH_LONG).show()
            }
        }

        btnTablaturas.setOnClickListener {
            if (isPremium) {
                startActivity(Intent(this, TablaturasActivity::class.java))
            } else {
                Toast.makeText(this, "❌ Disponible solo en versión Premium (\$249 MXN)", Toast.LENGTH_LONG).show()
            }
        }

        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        btnStar.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("tipo", "sin_anuncios")
            startActivityForResult(intent, 100)
        }

        btnDiamante.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("tipo", "premium")
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            recreate()
        }
    }
}