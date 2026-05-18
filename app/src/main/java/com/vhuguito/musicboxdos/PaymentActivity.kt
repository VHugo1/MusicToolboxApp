package com.vhuguito.musicboxdos

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var btnPagar: Button
    private var tipo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        tvTitulo = findViewById(R.id.tv_titulo)
        tvDescripcion = findViewById(R.id.tv_descripcion)
        btnPagar = findViewById(R.id.btn_pagar)

        tipo = intent.getStringExtra("tipo") ?: ""

        when (tipo) {
            "sin_anuncios" -> {
                tvTitulo.text = "Sin Anuncios"
                tvDescripcion.text = "Elimina los anuncios permanentemente\nPrecio: \$149 MXN"
                btnPagar.text = "Pagar \$149"
            }
            "premium" -> {
                tvTitulo.text = "Versión Premium"
                tvDescripcion.text = "Todo desbloqueado + Sin anuncios\nPrecio: \$249 MXN"
                btnPagar.text = "Pagar \$249"
            }
        }

        btnPagar.setOnClickListener {
            val prefs = getSharedPreferences("premium", MODE_PRIVATE)
            when (tipo) {
                "sin_anuncios" -> {
                    prefs.edit().putBoolean("sin_anuncios", true).apply()
                    Toast.makeText(this, "¡Sin Anuncios activado!", Toast.LENGTH_LONG).show()
                }
                "premium" -> {
                    prefs.edit().putBoolean("sin_anuncios", true).apply()
                    prefs.edit().putBoolean("is_premium", true).apply()
                    Toast.makeText(this, "¡Versión Premium activada!", Toast.LENGTH_LONG).show()
                }
            }
            setResult(RESULT_OK)
            finish()
        }
    }
}