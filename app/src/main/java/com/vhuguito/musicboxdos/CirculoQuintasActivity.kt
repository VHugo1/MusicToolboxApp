package com.vhuguito.musicboxdos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class CirculoQuintasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var adView: AdView
    private lateinit var btnDiamante: ImageButton
    private lateinit var txtPremium: TextView
    private lateinit var imgPromocional: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circulo_quintas)

        MobileAds.initialize(this) {}

        btnBack = findViewById(R.id.btn_back)
        adView = findViewById(R.id.adView)
        btnDiamante = findViewById(R.id.btn_diamante)
        txtPremium = findViewById(R.id.txt_premium)
        imgPromocional = findViewById(R.id.imgPromocional)

        btnBack.setOnClickListener { finish() }

        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val isPremium = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("is_premium", false)

        if (isPremium) {
            adView.visibility = View.GONE
            btnDiamante.visibility = View.GONE
            txtPremium.visibility = View.GONE
            imgPromocional.visibility = View.VISIBLE
        } else {
            adView.visibility = View.VISIBLE
            btnDiamante.visibility = View.VISIBLE
            txtPremium.visibility = View.VISIBLE
            imgPromocional.visibility = View.GONE
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            btnDiamante.setOnClickListener {
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("tipo", "premium")
                startActivityForResult(intent, 101)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            recreate()
        }
    }
}