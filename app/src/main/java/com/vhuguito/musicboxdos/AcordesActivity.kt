package com.vhuguito.musicboxdos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AcordesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtNombreAcorde: TextView
    private lateinit var diagramaImageView: ImageView
    private lateinit var diagramaErrorText: TextView
    private lateinit var btnCirculoQuintas: TextView

    private var notaActual = "C"
    private var tipoActual = "Mayor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acordes)

        initViews()
        setupListeners()
        actualizarAcorde()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        txtNombreAcorde = findViewById(R.id.txt_nombre_acorde)
        diagramaImageView = findViewById(R.id.diagrama_acorde)
        diagramaErrorText = findViewById(R.id.diagrama_error)
        btnCirculoQuintas = findViewById(R.id.btn_circulo_quintas)

        // Botones de notas - Fila 1 (C, D, E, F)
        findViewById<Button>(R.id.btn_nota_c).setOnClickListener { seleccionarNota("C") }
        findViewById<Button>(R.id.btn_nota_d).setOnClickListener { seleccionarNota("D") }
        findViewById<Button>(R.id.btn_nota_e).setOnClickListener { seleccionarNota("E") }
        findViewById<Button>(R.id.btn_nota_f).setOnClickListener { seleccionarNota("F") }

        // Botones de notas - Fila 2 (G, A, B)
        findViewById<Button>(R.id.btn_nota_g).setOnClickListener { seleccionarNota("G") }
        findViewById<Button>(R.id.btn_nota_a).setOnClickListener { seleccionarNota("A") }
        findViewById<Button>(R.id.btn_nota_b).setOnClickListener { seleccionarNota("B") }

        // Botones de tipo
        findViewById<Button>(R.id.btn_tipo_mayor).setOnClickListener { seleccionarTipo("Mayor") }
        findViewById<Button>(R.id.btn_tipo_menor).setOnClickListener { seleccionarTipo("menor") }
        findViewById<Button>(R.id.btn_tipo_septima).setOnClickListener { seleccionarTipo("7ª") }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnCirculoQuintas.setOnClickListener {
            val intent = Intent(this, CirculoQuintasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun seleccionarNota(nota: String) {
        notaActual = nota
        actualizarAcorde()
    }

    private fun seleccionarTipo(tipo: String) {
        tipoActual = tipo
        actualizarAcorde()
    }

    private fun actualizarAcorde() {
        txtNombreAcorde.text = "$notaActual $tipoActual"

        val nombreImagen = when (tipoActual) {
            "Mayor" -> obtenerNombreMayor(notaActual)
            "menor" -> obtenerNombreMenor(notaActual)
            "7ª" -> obtenerNombreSeptima(notaActual)
            else -> null
        }

        val resourceId = nombreImagen?.let {
            resources.getIdentifier(it, "drawable", packageName)
        } ?: 0

        if (resourceId != 0) {
            diagramaImageView.setImageResource(resourceId)
            diagramaImageView.visibility = View.VISIBLE
            diagramaErrorText.visibility = View.GONE
        } else {
            diagramaImageView.visibility = View.GONE
            diagramaErrorText.text = "Diagrama no disponible: $notaActual $tipoActual"
            diagramaErrorText.visibility = View.VISIBLE
        }
    }

    private fun obtenerNombreMayor(nota: String): String {
        return when (nota) {
            "C" -> "c_do_mayor"
            "D" -> "d_re_mayor"
            "E" -> "e_mi_mayor"
            "F" -> "f_fa_mayor"
            "G" -> "g_sol_mayor"
            "A" -> "a_la_mayor"
            "B" -> "b_si_mayor"
            else -> ""
        }
    }

    private fun obtenerNombreMenor(nota: String): String {
        return when (nota) {
            "C" -> "cm_do_menor"
            "D" -> "dm_re_menor"
            "E" -> "em_mi_menor"
            "F" -> "fm_fa_menor"
            "G" -> "gm_sol_menor"
            "A" -> "am_la_menor"
            "B" -> "bm_si_menor"
            else -> ""
        }
    }

    private fun obtenerNombreSeptima(nota: String): String {
        return when (nota) {
            "C" -> "c7_do7"
            "D" -> "d7_re7"
            "E" -> "e7_mi7"
            "F" -> "f7_fa7"
            "G" -> "g7_sol7"
            "A" -> "a7_la7"
            "B" -> "b7_si7"
            else -> ""
        }
    }
}