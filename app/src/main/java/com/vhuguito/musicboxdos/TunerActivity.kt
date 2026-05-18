package com.vhuguito.musicboxdos

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest

class TunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnMic: ImageButton
    private lateinit var barraSonido: ProgressBar
    private lateinit var cuadroNotaBaja: TextView
    private lateinit var txtNotaClasica: TextView
    private lateinit var txtNotaModerna: TextView
    private lateinit var cuadroNotaAlta: TextView
    private lateinit var txtRuido: TextView
    private lateinit var txtFrecuencia: TextView
    private lateinit var semicirculoView: SemicircleView
    private lateinit var btnStar: ImageButton
    private lateinit var btnDiamante: ImageButton
    private lateinit var btnPerfil: ImageButton
    private lateinit var txtSinAnuncios: TextView
    private lateinit var txtPremium: TextView
    private var smoothCents = 0
    private var lastCents = 0
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    private val THRESHOLD = 0.01

    private val notas = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val notasClasicas = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val frecuenciasBase = listOf(16.35, 17.32, 18.35, 19.45, 20.60, 21.83, 23.12, 24.50, 25.96, 27.50, 29.14, 30.87)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tuner)

        MobileAds.initialize(this) {}

        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        initViews()
        setupClickListeners()

        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val sinAnuncios = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("sin_anuncios", false)
        val isPremium = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("is_premium", false)

        txtSinAnuncios = findViewById(R.id.txt_sin_anuncios)
        txtPremium = findViewById(R.id.txt_premium)

        if (sinAnuncios || isPremium) {
            adView.visibility = View.GONE
            btnStar.visibility = View.GONE
            btnDiamante.visibility = View.GONE
            txtSinAnuncios.visibility = View.GONE
            txtPremium.visibility = View.GONE
            btnPerfil.visibility = View.GONE
        } else {
            adView.visibility = View.VISIBLE
            btnStar.visibility = View.VISIBLE
            btnDiamante.visibility = View.VISIBLE
            txtSinAnuncios.visibility = View.VISIBLE
            txtPremium.visibility = View.VISIBLE
            btnPerfil.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnMic = findViewById(R.id.btn_mic)
        barraSonido = findViewById(R.id.barra_sonido)
        cuadroNotaBaja = findViewById(R.id.cuadro_nota_baja)
        txtNotaClasica = findViewById(R.id.txt_nota_clasica)
        txtNotaModerna = findViewById(R.id.txt_nota_moderna)
        cuadroNotaAlta = findViewById(R.id.cuadro_nota_alta)
        txtRuido = findViewById(R.id.txt_ruido)
        txtFrecuencia = findViewById(R.id.txt_frecuencia)
        semicirculoView = findViewById(R.id.semicirculo_afinacion)
        btnStar = findViewById(R.id.btn_star)
        btnDiamante = findViewById(R.id.btn_diamante)
        btnPerfil = findViewById(R.id.btn_perfil)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnMic.setOnClickListener {
            if (isRecording) {
                stopRecording()
                btnMic.colorFilter = null
                barraSonido.progress = 0
            } else {
                if (checkPermission()) {
                    startRecording()
                    btnMic.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                } else {
                    requestPermission()
                }
            }
        }

        btnStar.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivityForResult(intent, 100)
        }

        btnDiamante.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivityForResult(intent, 100)
        }

        btnPerfil.setOnClickListener {
            // TODO: Abrir pantalla de perfil
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val adView = findViewById<AdView>(R.id.adView)
            adView.visibility = View.GONE
            val prefs = getSharedPreferences("premium", MODE_PRIVATE)
            prefs.edit().putBoolean("sin_anuncios", true).apply()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 200)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
                btnMic.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            } else {
                Toast.makeText(this, "Se necesita permiso para usar el micrófono", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )
        audioRecord?.startRecording()
        isRecording = true

        updateRunnable = Runnable {
            if (isRecording) {
                detectPitch()
                handler.postDelayed(updateRunnable!!, 50)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        updateRunnable?.let {
            handler.removeCallbacks(it)
        }
        updateRunnable = null
    }

    private fun detectPitch() {
        if (audioRecord == null) return

        val buffer = ShortArray(BUFFER_SIZE)
        val bytesRead = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0

        if (bytesRead <= 0) {
            runOnUiThread {
                txtRuido.text = "Error en el micrófono"
            }
            return
        }

        val maxAmplitude = buffer.maxOrNull()?.toDouble() ?: 0.0
        val nivel = (maxAmplitude / 32768.0).coerceIn(0.0, 1.0)
        val nivelPorcentaje = (nivel * 100).toInt()

        runOnUiThread {
            barraSonido.progress = nivelPorcentaje.coerceIn(0, 100)
        }

        if (!isTone(buffer) || maxAmplitude < 1000) {
            runOnUiThread {
                txtRuido.text = "Se percibe ruido"
                txtFrecuencia.text = "Frecuencia: 0 Hz"
                txtNotaModerna.text = "--"
                txtNotaClasica.text = "---"
                cuadroNotaBaja.text = "--"
                cuadroNotaAlta.text = "--"
            }
            return
        }

        val frequency = autoCorrelate(buffer, SAMPLE_RATE)
        val isValidFrequency = frequency > 60 && frequency < 5000

        if (isValidFrequency && frequency > 0) {
            runOnUiThread {
                txtFrecuencia.text = "Frecuencia: ${frequency.toInt()} Hz"
                txtRuido.text = ""
                val notaInfo = getNoteFromFrequency(frequency)
                updateNoteDisplay(notaInfo)
                updateSemicircle(notaInfo.centOffset)
            }
        } else {
            runOnUiThread {
                txtRuido.text = "Se percibe ruido"
                txtFrecuencia.text = "Frecuencia: 0 Hz"
            }
        }
    }

    private fun autoCorrelate(buffer: ShortArray, sampleRate: Int): Double {
        val size = buffer.size
        var maxCorrelation = 0.0
        var bestPitch = -1

        for (p in 20..1000) {
            var correlation = 0.0
            for (i in 0 until size - p) {
                correlation += buffer[i] * buffer[i + p]
            }
            correlation /= (size - p)
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation
                bestPitch = p
            }
        }

        return if (maxCorrelation > THRESHOLD && bestPitch > 0) {
            sampleRate.toDouble() / bestPitch
        } else {
            -1.0
        }
    }

    private fun isTone(buffer: ShortArray): Boolean {
        var maxAmplitude = 0.0
        var sum = 0.0

        for (sample in buffer) {
            val absSample = kotlin.math.abs(sample.toDouble())
            if (absSample > maxAmplitude) {
                maxAmplitude = absSample
            }
            sum += absSample
        }

        val avgAmplitude = sum / buffer.size

        if (maxAmplitude < 1000) return false

        var variance = 0.0
        for (sample in buffer) {
            val diff = kotlin.math.abs(sample.toDouble()) - avgAmplitude
            variance += diff * diff
        }
        variance /= buffer.size
        val stdDev = kotlin.math.sqrt(variance)

        return stdDev / avgAmplitude < 1.5
    }

    data class NoteInfo(val name: String, val classicName: String, val frequency: Double, val centOffset: Int)

    private fun getNoteFromFrequency(freq: Double): NoteInfo {
        var bestNote = 0
        var bestDiff = Double.MAX_VALUE
        var bestFreq = 0.0

        for (octave in 0..8) {
            for (i in notas.indices) {
                val noteFreq = frecuenciasBase[i] * (1 shl octave)
                val diff = kotlin.math.abs(freq - noteFreq)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestNote = i
                    bestFreq = noteFreq
                }
            }
        }

        val cents = -(1200 * kotlin.math.log2(freq / bestFreq)).toInt()
        val centsLimitado = cents.coerceIn(-50, 50)

        android.util.Log.d("TUNER", "freq=$freq, bestFreq=$bestFreq, cents=$cents")

            return NoteInfo(notas[bestNote], notasClasicas[bestNote], bestFreq, centsLimitado)
    }

    private fun updateNoteDisplay(noteInfo: NoteInfo) {
        txtNotaModerna.text = noteInfo.name
        txtNotaClasica.text = noteInfo.classicName

        val indexBaja = (notas.indexOf(noteInfo.name) - 1 + 12) % 12
        cuadroNotaBaja.text = notas[indexBaja]

        val indexAlta = (notas.indexOf(noteInfo.name) + 1) % 12
        cuadroNotaAlta.text = notas[indexAlta]
    }

    private fun updateSemicircle(cents: Int) {
        android.util.Log.d("TUNER", "updateSemicircle recibió cents: $cents")
        if (::semicirculoView.isInitialized) {
            semicirculoView.updateCents(cents)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun log2(x: Double): Double = kotlin.math.ln(x) / kotlin.math.ln(2.0)
}