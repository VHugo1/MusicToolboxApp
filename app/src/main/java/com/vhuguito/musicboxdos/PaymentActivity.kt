package com.vhuguito.musicboxdos

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var btnPagar: Button
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        tvTitulo = findViewById(R.id.tv_titulo)
        tvDescripcion = findViewById(R.id.tv_descripcion)
        btnPagar = findViewById(R.id.btn_pagar)

        tvTitulo.text = "Versión Premium"
        tvDescripcion.text = "✓ Sin anuncios\n✓ Acceso a todas las pantallas\n✓ Círculo de Quintas\n✓ Tablaturas de Piano\n✓ Acordes completos\n\nPrecio: \$249 MXN"
        btnPagar.text = "Comprar Premium \$249"

        // Inicializar BillingManager
        billingManager = BillingManager(this) {
            setResult(RESULT_OK)
            finish()
        }
        billingManager.startConnection()

        btnPagar.setOnClickListener {
            billingManager.startPurchase(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos si es necesario
    }
}