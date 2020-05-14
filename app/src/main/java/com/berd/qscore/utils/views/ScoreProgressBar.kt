package com.berd.qscore.utils.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.berd.qscore.R
import com.berd.qscore.utils.extensions.dpToPixels
import kotlin.math.roundToInt

class ScoreProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dm = resources.displayMetrics
    private var progressBackgroundWidthPixels: Float
    private var progressWidthPixels: Float
    private var progressColorStart: Int
    private var progressColorMiddle: Int
    private var progressColorEnd: Int
    private var progressTextColor: Int
    private var progressTextSize: Float
    private var progressBackgroundColor: Int

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScoreProgressBar,
            0, 0
        ).apply {
            try {
                progressBackgroundWidthPixels = getDimensionPixelSize(R.styleable.ScoreProgressBar_progress_background_width, 10).toFloat()
                progressBackgroundColor = getColor(R.styleable.ScoreProgressBar_progress_background_color, Color.LTGRAY)
                progressWidthPixels = getDimensionPixelSize(R.styleable.ScoreProgressBar_progress_width, 10).toFloat()
                progressColorStart = getColor(R.styleable.ScoreProgressBar_progress_color_start, Color.GREEN)
                progressColorMiddle = getColor(R.styleable.ScoreProgressBar_progress_color_middle, Color.GREEN)
                progressColorEnd = getColor(R.styleable.ScoreProgressBar_progress_color_end, Color.GREEN)
                progressTextColor = getColor(R.styleable.ScoreProgressBar_progress_text_color, Color.DKGRAY)
                progressTextSize = getDimensionPixelSize(R.styleable.ScoreProgressBar_progress_text_size, 100).toFloat()
            } finally {
                recycle()
            }
        }
    }

    private val backgroundPaint = Paint().apply {
        color = progressBackgroundColor
        style = Paint.Style.STROKE
        strokeWidth = progressBackgroundWidthPixels
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = progressColorStart
        style = Paint.Style.STROKE
        strokeWidth = progressWidthPixels
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val textPaint = TextPaint().apply {
        color = progressTextColor
        isAntiAlias = true
        textSize = progressTextSize
        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    }

    var progress: Float = 0f
        set(value) {
            var animator = ValueAnimator.ofFloat(progress, value).apply {
                duration = 750
                addUpdateListener { updatedAnimation ->
                    field = updatedAnimation.animatedValue as Float
                    updateProgressPaint()
                    invalidate()
                }
                start()
            }
            invalidate()
        }

    private fun updateProgressPaint() {
        if (progress < 0.5f) {
            val red = ((progressColorMiddle.red - progressColorStart.red) * progress * 2 + progressColorStart.red).toInt()
            val green = ((progressColorMiddle.green - progressColorStart.green) * progress * 2 + progressColorStart.green).toInt()
            val blue = ((progressColorMiddle.blue - progressColorStart.blue) * progress * 2 + progressColorStart.blue).toInt()
            val alpha = ((progressColorMiddle.alpha - progressColorStart.alpha) * progress * 2 + progressColorStart.alpha).toInt()
            progressPaint.color = Color.argb(alpha, red, green, blue)
        } else {
            val red = ((progressColorEnd.red - progressColorMiddle.red) * (progress - 0.5f) * 2 + progressColorMiddle.red).toInt()
            val green = ((progressColorEnd.green - progressColorMiddle.green) * (progress - 0.5f) * 2 + progressColorMiddle.green).toInt()
            val blue = ((progressColorEnd.blue - progressColorMiddle.blue) * (progress - 0.5f) * 2 + progressColorMiddle.blue).toInt()
            val alpha = ((progressColorEnd.alpha - progressColorMiddle.alpha) * (progress - 0.5f) * 2 + progressColorMiddle.alpha).toInt()
            progressPaint.color = Color.argb(alpha, red, green, blue)
        }
    }

    fun getBackgroundWidthPixels(): Float {
        return progressBackgroundWidthPixels
    }

    fun setBackgroundWidthPixels(width: Float) {
        progressBackgroundWidthPixels = width
        invalidate()
        requestLayout()
    }

    fun getProgressWidthPixels(): Float {
        return progressWidthPixels
    }

    fun setProgressWidthPixels(width: Float) {
        progressWidthPixels = width
        updateRadius()
        updateMaxProgress()
        invalidate()
        requestLayout()
    }

    private val oval = RectF()
    private val textBounds = Rect()
    private var centerX: Float = 100f
    private var centerY: Float = 100f
    private var radius: Float = 50f
    private var maxProgress: Float = 1f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min, min)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w.toFloat() / 2
        centerY = h.toFloat() / 2
        updateRadius()
        updateMaxProgress()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun updateRadius() {
        radius = centerX - progressWidthPixels
        oval.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    private fun updateMaxProgress() {
        val paddingDP = 1f
        val paddingPixels = paddingDP.dpToPixels(dm)
        val minSliver = Math.toDegrees(((progressWidthPixels + paddingPixels) / radius).toDouble()).toFloat()
        maxProgress = (360f - minSliver) / 360f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)
        val finalProgress: Float = when {
            (progress == 1f) -> progress
            (progress > maxProgress) -> maxProgress
            else -> progress
        }
        canvas?.drawArc(oval, 270f, 360f * finalProgress, false, progressPaint)
        val score: String = (100 * progress).roundToInt().toString()
        textPaint.getTextBounds(score, 0, score.length, textBounds)
        canvas?.drawText(score, centerX - textBounds.exactCenterX(), centerY - textBounds.exactCenterY(), textPaint)
    }
}
