package com.vhuguito.musicboxdos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TablaturasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var imagenTablatura: ImageView
    private lateinit var txtError: TextView
    private lateinit var btnCirculoQuintas: ImageButton

    private var tipoActual = "Mayores"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tablaturas)

        initViews()
        setupListeners()
        actualizarTablatura()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        imagenTablatura = findViewById(R.id.imagen_tablatura)
        txtError = findViewById(R.id.txt_error)
        btnCirculoQuintas = findViewById(R.id.btn_circulo_quintas)

        findViewById<Button>(R.id.btn_mayores).setOnClickListener { seleccionarTipo("Mayores") }
        findViewById<Button>(R.id.btn_menores).setOnClickListener { seleccionarTipo("menores") }
        findViewById<Button>(R.id.btn_septima_mayor).setOnClickListener { seleccionarTipo("septima_mayor") }
        findViewById<Button>(R.id.btn_septima_menor).setOnClickListener { seleccionarTipo("septima_menor") }
        findViewById<Button>(R.id.btn_novena_mayor).setOnClickListener { seleccionarTipo("novena_mayor") }
        findViewById<Button>(R.id.btn_novena_dominante).setOnClickListener { seleccionarTipo("novena_dominante") }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnCirculoQuintas.setOnClickListener {
            val intent = Intent(this, CirculoQuintasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun seleccionarTipo(tipo: String) {
        tipoActual = tipo
        actualizarTablatura()
    }

    private fun actualizarTablatura() {
        val nombreImagen = when (tipoActual) {
            "Mayores" -> "acordes_mayores_piano"
            "menores" -> "acordes_menores_piano"
            "septima_mayor" -> "acordes_septima_mayor"
            "septima_menor" -> "acordes_septima_menor"
            "novena_mayor" -> "acordes_novena_mayor"
            "novena_dominante" -> "acordes_novena_dominante"
            else -> null
        }

        val resourceId = nombreImagen?.let {
            resources.getIdentifier(it, "drawable", packageName)
        } ?: 0

        if (resourceId != 0) {
            imagenTablatura.setImageResource(resourceId)
            imagenTablatura.visibility = View.VISIBLE
            txtError.visibility = View.GONE
        } else {
            imagenTablatura.visibility = View.GONE
            txtError.text = "Tablatura no disponible"
            txtError.visibility = View.VISIBLE
        }
    }
}