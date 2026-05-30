package com.vhuguito.musicboxdos

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.pow

class MetronomeActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvBPMValue: TextView
    private lateinit var seekBarBpm: SeekBar
    private lateinit var seekBarVolume: SeekBar
    private lateinit var btnTime24: Button
    private lateinit var btnTime34: Button
    private lateinit var btnTime44: Button
    private lateinit var btnTime68: Button
    private lateinit var beatIndicators: LinearLayout
    private lateinit var btnTapDown: Button
    private lateinit var btnTapUp: Button
    private lateinit var btnDiamante: ImageButton
    private lateinit var txtPremium: TextView
    private lateinit var adView: AdView
    private lateinit var btnCirculoQuintas: TextView
    private lateinit var imgPromocional: ImageView   // <--- Agregado

    private var bpm = 120
    private var isPlaying = false
    private var currentBeat = 0
    private var beatsPerMeasure = 4
    private val handler = Handler(Looper.getMainLooper())
    private var tickRunnable: Runnable? = null
    private var volume = 0.8f

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metronome)

        MobileAds.initialize(this) {}

        initViews()
        setupListeners()
        updateBeatIndicators()

        adView = findViewById(R.id.adView)
        btnCirculoQuintas = findViewById(R.id.btn_circulo_quintas)
        imgPromocional = findViewById(R.id.imgPromocional)   // <--- Inicializar imagen

        val prefs = getSharedPreferences("premium", MODE_PRIVATE)
        val isPremium = DebugConfig.FORZAR_PREMIUM || prefs.getBoolean("is_premium", false)

        if (isPremium) {
            adView.visibility = View.GONE
            btnDiamante.visibility = View.GONE
            txtPremium.visibility = View.GONE
            imgPromocional.visibility = View.VISIBLE   // <--- Mostrar imagen
        } else {
            adView.visibility = View.VISIBLE
            btnDiamante.visibility = View.VISIBLE
            txtPremium.visibility = View.VISIBLE
            imgPromocional.visibility = View.GONE      // <--- Ocultar imagen
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
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        tvBPMValue = findViewById(R.id.tv_bpm_value)
        seekBarBpm = findViewById(R.id.seekbar_bpm)
        seekBarVolume = findViewById(R.id.seekbar_volume)
        btnTime24 = findViewById(R.id.btn_time_2_4)
        btnTime34 = findViewById(R.id.btn_time_3_4)
        btnTime44 = findViewById(R.id.btn_time_4_4)
        btnTime68 = findViewById(R.id.btn_time_6_8)
        beatIndicators = findViewById(R.id.beat_indicators)
        btnTapDown = findViewById(R.id.btn_tap_down)
        btnTapUp = findViewById(R.id.btn_tap_up)
        btnDiamante = findViewById(R.id.btn_diamante)
        txtPremium = findViewById(R.id.txt_premium)

        seekBarBpm.progress = bpm - 20
        tvBPMValue.text = bpm.toString()
        seekBarVolume.progress = (volume * 100).toInt()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnPlayPause.setOnClickListener {
            if (isPlaying) stopMetronome() else startMetronome()
        }

        seekBarBpm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    bpm = progress + 20
                    tvBPMValue.text = bpm.toString()
                    if (isPlaying) restartMetronome()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) volume = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnTapDown.setOnClickListener {
            if (bpm > 40) {
                bpm--
                tvBPMValue.text = bpm.toString()
                seekBarBpm.progress = bpm - 20
                if (isPlaying) restartMetronome()
            }
        }

        btnTapUp.setOnClickListener {
            if (bpm < 300) {
                bpm++
                tvBPMValue.text = bpm.toString()
                seekBarBpm.progress = bpm - 20
                if (isPlaying) restartMetronome()
            }
        }

        btnTime24.setOnClickListener { setTimeSignature(2) }
        btnTime34.setOnClickListener { setTimeSignature(3) }
        btnTime44.setOnClickListener { setTimeSignature(4) }
        btnTime68.setOnClickListener { setTimeSignature(6) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            recreate()
        }
    }

    private fun setTimeSignature(beats: Int) {
        beatsPerMeasure = beats
        updateBeatIndicators()
        if (isPlaying) restartMetronome()
    }

    private fun updateBeatIndicators() {
        beatIndicators.removeAllViews()
        for (i in 0 until beatsPerMeasure) {
            val beatView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    marginEnd = 12
                    marginStart = 12
                }
                background = getDrawable(R.drawable.circle_beat)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                tag = i
            }
            beatIndicators.addView(beatView)
        }
    }

    private fun highlightBeat(beat: Int) {
        for (i in 0 until beatIndicators.childCount) {
            val child = beatIndicators.getChildAt(i)
            if (i == beat) {
                if (i == 0) {
                    child.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
                } else {
                    child.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                }
            } else {
                child.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun generateClaveSound(amplitude: Float): ShortArray {
        val duration = 0.08
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val envelope = (1.0 - (i.toDouble() / numSamples).pow(0.5)).toFloat()
            val t = i.toDouble() / sampleRate
            val freq1 = 1200.0
            val freq2 = 400.0
            val value = amplitude * envelope * (sin(2 * PI * freq1 * t) + 0.5 * sin(2 * PI * freq2 * t))
            samples[i] = (value * 32767).toInt().toShort()
        }
        return samples
    }

    private fun playSound(isAccent: Boolean) {
        try {
            val amplitude = if (isAccent) volume else volume * 0.6f
            val samples = generateClaveSound(amplitude)
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            audioTrack?.write(samples, 0, samples.size)
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startMetronome() {
        isPlaying = true
        btnPlayPause.setColorFilter(android.graphics.Color.parseColor("#00FF00"))
        currentBeat = 0
        scheduleTick()
    }

    private fun stopMetronome() {
        isPlaying = false
        btnPlayPause.colorFilter = null
        tickRunnable?.let { handler.removeCallbacks(it) }
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        highlightBeat(-1)
    }

    private fun restartMetronome() {
        if (isPlaying) {
            stopMetronome()
        }
        handler.postDelayed({
            startMetronome()
        }, 50)
    }

    private fun scheduleTick() {
        val interval = (60000.0 / bpm).toLong()
        tickRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    playTick()
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(tickRunnable!!)
    }

    private fun playTick() {
        val isAccent = (currentBeat % beatsPerMeasure) == 0
        playSound(isAccent)
        highlightBeat(currentBeat % beatsPerMeasure)
        currentBeat++
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMetronome()
    }
}