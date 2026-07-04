package com.example.appactivitytracker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5C6BC0")
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3D5AFE")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A2A2A")
        strokeWidth = 1f
    }
    private val gridTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        textSize = 24f
        textAlign = Paint.Align.RIGHT
    }

    private var data: List<Long> = emptyList()
    private var highlightIndex: Int = -1
    private val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    fun setData(data: List<Long>, highlightIndex: Int = -1) {
        this.data = data
        this.highlightIndex = highlightIndex
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val bottomPadding = 50f
        val topPadding = 20f
        val leftPadding = 60f
        val rightPadding = 16f
        val chartHeight = h - bottomPadding - topPadding
        val chartWidth = w - leftPadding - rightPadding

        val maxVal = data.maxOrNull()?.takeIf { it > 0 } ?: 1L

        // Draw grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = topPadding + chartHeight - (chartHeight * i / gridLines)
            canvas.drawLine(leftPadding, y, w - rightPadding, y, gridPaint)
            val hours = (maxVal / (1000 * 60 * 60).toDouble() * i / gridLines).toInt()
            canvas.drawText("${hours}h", leftPadding - 8, y + 8, gridTextPaint)
        }

        // Draw bars
        val barCount = data.size
        val barWidth = (chartWidth / barCount) * 0.5f
        val spacing = chartWidth / barCount

        data.forEachIndexed { index, value ->
            val barHeight = if (maxVal > 0) (chartHeight * value / maxVal).toFloat() else 0f
            val x = leftPadding + spacing * index + spacing / 2f
            val top = topPadding + chartHeight - barHeight
            val bottom = topPadding + chartHeight

            val rect = RectF(x - barWidth / 2, top, x + barWidth / 2, bottom)
            val paint = if (index == highlightIndex) highlightPaint else barPaint
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            canvas.drawText(dayLabels.getOrElse(index) { "" }, x, h - 10f, textPaint)
        }
    }
}