package com.vhuguito.musicboxdos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var btnDiamante: ImageButton
    private lateinit var btnDiamanteText: TextView
    private lateinit var imgPromocional: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        btnDiamante = findViewById(R.id.btn_diamante)
        btnDiamanteText = findViewById(R.id.btn_diamante_text)
        imgPromocional = findViewById(R.id.imgPromocional)

        checkPurchaseStatus()

        val btnAfinador = findViewById<Button>(R.id.btn_afinador)
        val btnMetronomo = findViewById<Button>(R.id.btn_metronomo)
        val btnAcordes = findViewById<Button>(R.id.btn_acordes)
        val btnTablaturas = findViewById<Button>(R.id.btn_tablaturas)
        val btnCirculoQuintas = findViewById<Button>(R.id.btn_circulo_quintas)
        val btnPerfil = findViewById<ImageButton>(R.id.btn_perfil)

        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val isPremium = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("is_premium", false)

        btnAfinador.setOnClickListener {
            startActivity(Intent(this, TunerActivity::class.java))
        }

        btnMetronomo.setOnClickListener {
            startActivity(Intent(this, MetronomeActivity::class.java))
        }

        btnAcordes.setOnClickListener {
            if (isPremium) {
                startActivity(Intent(this, AcordesActivity::class.java))
            } else {
                Toast.makeText(this, "❌ Disponible solo en versión Premium", Toast.LENGTH_LONG).show()
            }
        }

        btnTablaturas.setOnClickListener {
            if (isPremium) {
                startActivity(Intent(this, TablaturasActivity::class.java))
            } else {
                Toast.makeText(this, "❌ Disponible solo en versión Premium", Toast.LENGTH_LONG).show()
            }
        }

        btnCirculoQuintas.setOnClickListener {
            if (isPremium) {
                startActivity(Intent(this, CirculoQuintasActivity::class.java))
            } else {
                Toast.makeText(this, "❌ Disponible solo en versión Premium", Toast.LENGTH_LONG).show()
            }
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        btnDiamante.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("tipo", "premium")
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            recreate()
        }
    }

    private fun checkPurchaseStatus() {
        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val isPremium = prefs.getBoolean("is_premium", false)

        if (isPremium) {
            adView.visibility = View.GONE
            btnDiamante.visibility = View.GONE
            btnDiamanteText.visibility = View.GONE
            imgPromocional.visibility = View.VISIBLE
        } else {
            adView.visibility = View.VISIBLE
            btnDiamante.visibility = View.VISIBLE
            btnDiamanteText.visibility = View.VISIBLE
            imgPromocional.visibility = View.GONE
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }
}