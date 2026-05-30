package com.vhuguito.musicboxdos

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CirculoQuintasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val notasMayores = listOf("C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#", "F")
    private val notasMenores = listOf("Am", "Em", "Bm", "F#m", "C#m", "G#m", "D#m", "A#m", "Fm", "Cm", "Gm", "Dm")
    private val notasDisplay = listOf("C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#", "F")

    private var ancho = 0f
    private var alto = 0f
    private var centroX = 0f
    private var centroY = 0f
    private var radio = 0f
    private var radioInterno = 0f

    private var anguloSeleccionado = -90f
    private var arrastrando = false

    private val colorFondoCirculo = Color.parseColor("#2C3E50")
    private val colorFondoInterno = Color.parseColor("#03FDEE")
    private val colorTexto = Color.parseColor("#000000")
    private val colorSelectorBorde = Color.parseColor("#000000")
    private val colorSelectorFondo = Color.parseColor("#33FFD700")
    private val colorBordeCirculo = Color.parseColor("#000000")
    private val colorNotaFondo = Color.parseColor("#FFFFFF")

    init {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 6f
        selectorPaint.color = colorSelectorBorde
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        ancho = w.toFloat()
        alto = h.toFloat()
        centroX = ancho / 2f
        centroY = alto / 2f
        radio = (Math.min(ancho, alto) / 2.2f).coerceAtMost(ancho / 2f)
        radioInterno = radio * 0.72f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Círculo exterior
        paint.color = colorFondoCirculo
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centroX, centroY, radio, paint)

        paint.color = colorBordeCirculo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(centroX, centroY, radio, paint)

        // Círculo interior
        paint.style = Paint.Style.FILL
        paint.color = colorFondoInterno
        canvas.drawCircle(centroX, centroY, radioInterno, paint)

        paint.style = Paint.Style.STROKE
        paint.color = colorBordeCirculo
        canvas.drawCircle(centroX, centroY, radioInterno, paint)

        // Dibujar notas
        textPaint.color = colorTexto

        for (i in notasMayores.indices) {
            val angulo = calcularAngulo(i)
            val x = centroX + radio * Math.cos(angulo).toFloat()
            val y = centroY + radio * Math.sin(angulo).toFloat()

            paint.color = colorNotaFondo
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, radio / 6.5f, paint)

            paint.color = colorBordeCirculo
            paint.style = Paint.Style.STROKE
            canvas.drawCircle(x, y, radio / 6.5f, paint)

            textPaint.textSize = radio / 8f
            canvas.drawText(notasDisplay[i], x, y + textPaint.textSize / 3f, textPaint)

            val xMenor = centroX + radioInterno * 0.85f * Math.cos(angulo).toFloat()
            val yMenor = centroY + radioInterno * 0.85f * Math.sin(angulo).toFloat()
            textPaint.textSize = radio / 10f
            canvas.drawText(notasMenores[i], xMenor, yMenor + textPaint.textSize / 3f, textPaint)
        }

        // Pentágono selector - Base reducida a la mitad
        val radSeleccion = Math.toRadians(anguloSeleccionado.toDouble())

        // Ángulos del pentágono (lados superiores se mantienen)
        val anguloPunta = radSeleccion
        val anguloLadoDer = radSeleccion + Math.toRadians(38.0)
        val anguloLadoIzq = radSeleccion - Math.toRadians(38.0)
        val anguloBaseDer = radSeleccion + Math.toRadians(72.0)
        val anguloBaseIzq = radSeleccion - Math.toRadians(72.0)

        // Radios: punta y laterales largos, base REDUCIDA A LA MITAD
        val radioPunta = radio * 1.15f
        val radioLateral = radio * 1.10f
        val radioBase = radio * 0.30f   // Base reducida a la mitad

        val path = Path()

        val puntaX = centroX + radioPunta * Math.cos(anguloPunta).toFloat()
        val puntaY = centroY + radioPunta * Math.sin(anguloPunta).toFloat()
        path.moveTo(puntaX, puntaY)

        val ladoDerX = centroX + radioLateral * Math.cos(anguloLadoDer).toFloat()
        val ladoDerY = centroY + radioLateral * Math.sin(anguloLadoDer).toFloat()
        path.lineTo(ladoDerX, ladoDerY)

        val baseDerX = centroX + radioBase * Math.cos(anguloBaseDer).toFloat()
        val baseDerY = centroY + radioBase * Math.sin(anguloBaseDer).toFloat()
        path.lineTo(baseDerX, baseDerY)

        val baseIzqX = centroX + radioBase * Math.cos(anguloBaseIzq).toFloat()
        val baseIzqY = centroY + radioBase * Math.sin(anguloBaseIzq).toFloat()
        path.lineTo(baseIzqX, baseIzqY)

        val ladoIzqX = centroX + radioLateral * Math.cos(anguloLadoIzq).toFloat()
        val ladoIzqY = centroY + radioLateral * Math.sin(anguloLadoIzq).toFloat()
        path.lineTo(ladoIzqX, ladoIzqY)

        path.close()

        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = colorSelectorFondo
        canvas.drawPath(path, selectorPaint)

        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 6f
        selectorPaint.color = colorSelectorBorde
        canvas.drawPath(path, selectorPaint)

        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 6f
        canvas.drawLine(puntaX, puntaY, centroX, centroY, selectorPaint)
    }

    private fun calcularAngulo(indice: Int): Double {
        return Math.toRadians((-90 + indice * 30).toDouble())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - centroX
                val dy = event.y - centroY
                val distancia = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (distancia > radio * 0.85f) {
                    arrastrando = true
                    actualizarAngulo(event.x, event.y)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (arrastrando) {
                    actualizarAngulo(event.x, event.y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                arrastrando = false
            }
        }
        return super.onTouchEvent(event)
    }

    private fun actualizarAngulo(x: Float, y: Float) {
        val dx = x - centroX
        val dy = y - centroY
        var angulo = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        angulo = (angulo + 360) % 360
        anguloSeleccionado = angulo
        invalidate()
    }
}