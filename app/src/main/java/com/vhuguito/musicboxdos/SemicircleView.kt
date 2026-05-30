package com.vhuguito.musicboxdos

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class SemicircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cents = 0

    private val colores = listOf(
        "#8B0000", "#FF4500", "#FFA500", "#FFFF00", "#00FF00",
        "#FFFF00", "#FFA500", "#FF4500", "#8B0000"
    )

    fun updateCents(nuevoCents: Int) {
        android.util.Log.d("SemicircleView", "updateCents llamado con: $nuevoCents")
        cents = nuevoCents.coerceIn(-50, 50)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        paint.color = Color.TRANSPARENT
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, paint)

        val cx = w / 2f
        val cy = h * 0.90f
        val radio = w / 2.2f

        val rect = RectF(cx - radio, cy - radio, cx + radio, cy + radio)

        // Dibujar franjas
        for (i in colores.indices) {
            paint.color = Color.parseColor(colores[i])
            paint.style = Paint.Style.FILL
            canvas.drawArc(rect, 180f + (i * 20f), 20f, true, paint)
        }

        // Líneas divisorias
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f

        for (i in 0..9) {
            val angulo = 180f + (i * 20f)
            val rad = Math.toRadians(angulo.toDouble())
            val x = cx + radio * cos(rad).toFloat()
            val y = cy + radio * sin(rad).toFloat()
            canvas.drawLine(cx, cy, x, y, paint)
        }

        // Números
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        for (i in 0 until 9) {
            val valor = -40 + (i * 10)
            val angulo = 180f + (i * 20f) + 10f
            val rad = Math.toRadians(angulo.toDouble())
            val radioTexto = radio * 0.7f
            val x = cx + radioTexto * cos(rad).toFloat()
            val y = cy + radioTexto * sin(rad).toFloat()
            canvas.drawText(valor.toString(), x, y, paint)
        }

        // AGUJA - Usando cents directamente
        // cents: -50 = -40 (extremo izquierdo), 0 = 0 (centro), +50 = +40 (extremo derecho)
        val anguloAguja = 180f + ((cents + 50f) / 100f) * 180f
        val radAguja = Math.toRadians(anguloAguja.toDouble())
        val largoAguja = radio * 0.75f
        val xAguja = cx + largoAguja * cos(radAguja).toFloat()
        val yAguja = cy + largoAguja * sin(radAguja).toFloat()

        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawLine(cx, cy, xAguja, yAguja, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        canvas.drawCircle(xAguja, yAguja, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        canvas.drawCircle(cx, cy, 14f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, 6f, paint)


    }
}