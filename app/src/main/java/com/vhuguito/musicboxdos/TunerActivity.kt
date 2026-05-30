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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class TunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnMic: ImageButton
    private lateinit var txtFrecuencia: TextView
    private lateinit var semicirculoView: SemicircleView
    private lateinit var txtNotaModerna: TextView
    private lateinit var txtNotaClasica: TextView
    private lateinit var txtRuido: TextView
    private lateinit var cuadroNotaBaja: TextView
    private lateinit var cuadroNotaAlta: TextView
    private lateinit var adView: AdView
    private lateinit var btnCirculoQuintas: TextView
    private lateinit var btnDiamante: ImageButton
    private lateinit var txtPremium: TextView
    private lateinit var imgPromocional: ImageView

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

        initViews()

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        btnCirculoQuintas = findViewById(R.id.btn_circulo_quintas)
        btnDiamante = findViewById(R.id.btn_diamante)
        txtPremium = findViewById(R.id.txt_premium)
        imgPromocional = findViewById(R.id.imgPromocional)

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

        btnCirculoQuintas.setOnClickListener {
            startActivity(Intent(this, CirculoQuintasActivity::class.java))
        }

        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnMic = findViewById(R.id.btn_mic)
        txtFrecuencia = findViewById(R.id.txt_frecuencia)
        semicirculoView = findViewById(R.id.semicirculo_afinacion)
        txtNotaModerna = findViewById(R.id.txt_nota_moderna)
        txtNotaClasica = findViewById(R.id.txt_nota_clasica)
        txtRuido = findViewById(R.id.txt_ruido)
        cuadroNotaBaja = findViewById(R.id.cuadro_nota_baja)
        cuadroNotaAlta = findViewById(R.id.cuadro_nota_alta)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnMic.setOnClickListener {
            if (isRecording) {
                stopRecording()
                btnMic.setColorFilter(null)
            } else {
                if (checkPermission()) {
                    startRecording()
                    btnMic.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                } else {
                    requestPermission()
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            recreate()
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
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun detectPitch() {
        if (audioRecord == null) return

        val buffer = ShortArray(BUFFER_SIZE)
        val bytesRead = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0

        if (bytesRead <= 0) return

        if (!isTone(buffer)) {
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
                semicirculoView.updateCents(notaInfo.centOffset)
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
            val absSample = abs(sample.toDouble())
            if (absSample > maxAmplitude) {
                maxAmplitude = absSample
            }
            sum += absSample
        }

        val avgAmplitude = sum / buffer.size

        if (maxAmplitude < 1000) return false

        var variance = 0.0
        for (sample in buffer) {
            val diff = abs(sample.toDouble()) - avgAmplitude
            variance += diff * diff
        }
        variance /= buffer.size
        val stdDev = sqrt(variance)

        return stdDev / avgAmplitude < 1.5
    }

    data class NoteInfo(val name: String, val classicName: String, val frequency: Double, val centOffset: Int)

    private fun getNoteFromFrequency(freq: Double): NoteInfo {
        var bestNote = 0
        var bestDiff = Double.MAX_VALUE
        var bestFreq = 0.0
        var bestOctave = 0

        for (octave in 0..8) {
            for (i in notas.indices) {
                val noteFreq = frecuenciasBase[i] * (1 shl octave)
                val diff = abs(freq - noteFreq)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestNote = i
                    bestFreq = noteFreq
                    bestOctave = octave
                }
            }
        }

        val cents = (1200 * ln(freq / bestFreq) / ln(2.0)).toInt()
        val centsLimitado = cents.coerceIn(-50, 50)

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

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}