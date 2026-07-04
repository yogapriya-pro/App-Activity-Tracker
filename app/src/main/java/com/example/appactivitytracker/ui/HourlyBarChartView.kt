package com.example.appactivitytracker.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class HourlyBarChartView @JvmOverloads constructor(
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
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A2A2A")
        strokeWidth = 1f
    }
    private val gridTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        textSize = 22f
        textAlign = Paint.Align.RIGHT
    }
    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
    }
    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private var data: List<Long> = emptyList()
    private var selectedHour: Int = -1
    private val hourLabels = listOf("2 AM", "4 AM", "8 AM", "12 PM", "4 PM", "8 PM", "12 AM")
    private val hourLabelIndices = listOf(2, 4, 8, 12, 16, 20, 24)

    fun setData(data: List<Long>) {
        this.data = data
        // Find the peak hour
        selectedHour = data.indices.maxByOrNull { data[it] } ?: -1
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val w = width.toFloat()
            val leftPadding = 60f
            val rightPadding = 16f
            val chartWidth = w - leftPadding - rightPadding
            val index = ((event.x - leftPadding) / chartWidth * data.size).toInt()
            if (index in data.indices) {
                selectedHour = index
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val bottomPadding = 50f
        val topPadding = 50f
        val leftPadding = 60f
        val rightPadding = 16f
        val chartHeight = h - bottomPadding - topPadding
        val chartWidth = w - leftPadding - rightPadding

        val maxVal = data.maxOrNull()?.takeIf { it > 0 } ?: 1L

        // Grid lines (3 levels)
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = topPadding + chartHeight - (chartHeight * i / gridLines)
            canvas.drawLine(leftPadding, y, w - rightPadding, y, gridPaint)
            val minutes = (maxVal / (1000 * 60).toDouble() * i / gridLines).toInt()
            if (minutes > 0) canvas.drawText("${minutes}m", leftPadding - 8, y + 8, gridTextPaint)
        }

        val barCount = data.size
        val barWidth = (chartWidth / barCount) * 0.6f
        val spacing = chartWidth / barCount

        data.forEachIndexed { index, value ->
            val barHeight = if (maxVal > 0) (chartHeight * value / maxVal).toFloat() else 0f
            val x = leftPadding + spacing * index + spacing / 2f
            val top = topPadding + chartHeight - barHeight
            val bottom = topPadding + chartHeight

            if (barHeight > 0) {
                val rect = RectF(x - barWidth / 2, top, x + barWidth / 2, bottom)
                val paint = if (index == selectedHour) highlightPaint else barPaint
                canvas.drawRoundRect(rect, 4f, 4f, paint)
            }
        }

        // Draw hour labels at specific intervals
        hourLabelIndices.forEachIndexed { i, hourIndex ->
            if (hourIndex <= data.size) {
                val x = leftPadding + spacing * hourIndex
                canvas.drawText(hourLabels[i], x, h - 10f, textPaint)
            }
        }

        // Draw tooltip for selected hour
        if (selectedHour in data.indices && data[selectedHour] > 0) {
            val minutes = data[selectedHour] / (1000 * 60)
            val timeLabel = formatHour(selectedHour)
            val tooltipText = if (minutes < 60) "${minutes}m • $timeLabel" else "${minutes / 60}h ${minutes % 60}m • $timeLabel"
            val x = leftPadding + spacing * selectedHour + spacing / 2f
            val tooltipW = tooltipTextPaint.measureText(tooltipText) + 24f
            val tooltipX = x.coerceIn(leftPadding + tooltipW / 2, w - rightPadding - tooltipW / 2)
            val rect = RectF(tooltipX - tooltipW / 2, 4f, tooltipX + tooltipW / 2, 38f)
            canvas.drawRoundRect(rect, 8f, 8f, tooltipBgPaint)
            canvas.drawText(tooltipText, tooltipX, 28f, tooltipTextPaint)
        }
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00 AM"
            hour < 12 -> "$hour:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }
}
