package com.berd.qscore.utils.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.berd.qscore.R
import com.berd.qscore.utils.extensions.dpToPixels
import kotlin.math.roundToInt

class ScoreProgressBar  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val backgroundWidthDP = 15f
    private val progressWidthDP = 15f
    private val dm  = resources.displayMetrics
    private val backgroundWidthPixels = backgroundWidthDP.dpToPixels(dm)
    private val progressWidthPixels = progressWidthDP.dpToPixels(dm)

    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = backgroundWidthPixels
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = context.getColor(R.color.colorPrimary)
        style = Paint.Style.STROKE
        strokeWidth = progressWidthPixels
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    var progress: Float = 0f
        set(value) {

            val finalProgress: Float = when {
                (value == 1f) -> value
                (value > maxProgress) -> maxProgress
                else -> value
            }

            var animator = ValueAnimator.ofFloat(progress, finalProgress).apply {
                duration = 750
                addUpdateListener { updatedAnimation ->
                    field = updatedAnimation.animatedValue as Float
                    //call updateProgressPaint if you want to updated colors when progress changes
                    invalidate()
                }
                start()
            }
            invalidate()
        }

    fun updateProgressPaint() {
        if (progress < 0.5f) {
            val green = progress * 2 * 255
            progressPaint.color = Color.rgb(255,green.roundToInt(),0)
        } else if (progress < 1.0f) {
            val red = (1.25 - progress / 2) * 255
            progressPaint.color = Color.rgb(red.roundToInt(),255,0)
        }  else {
            progressPaint.color = Color.rgb(0,255,0)
        }
    }

    private val oval = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var maxProgress: Float = 1f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min,min)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w.toFloat() / 2
        centerY = h.toFloat() / 2
        radius = w.toFloat() / 2 - progressWidthPixels
        oval.set(centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius)
        val paddingDP = 1f
        val paddingPixels = paddingDP.dpToPixels(dm)
        val minSliver = Math.toDegrees(((progressWidthPixels + paddingPixels) / radius).toDouble()).toFloat()
        maxProgress = (360f - minSliver) / 360f
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)
        canvas?.drawArc(oval, 270f, 360f * progress, false, progressPaint)
    }
}