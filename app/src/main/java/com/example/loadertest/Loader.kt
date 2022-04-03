package com.example.loadertest

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnRepeat
import kotlin.math.*

class Loader @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : View(context, attrs) {


    private var viewHeight: Float = 0f
    private var viewWidth: Float = 0f

    private var FIXED_RADIUS = 0f
    private var RADIUS_PART = 0f
    private val PADDING = 20f
    private val WIDTH = 20f

    private var CENTER_X = 0f
    private var CENTER_Y = 0f
    private var END_X = 0f
    private var END_Y = 0f

    private var boundaryPoints = mutableListOf<EndPoint>()
    private var isExpanding = true

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        strokeJoin = Paint.Join.BEVEL
        strokeWidth = WIDTH
    }

    private fun toRadian(angleInDegree: Float): Double {
        return angleInDegree * (PI / 180f)
    }

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1000
        interpolator = AccelerateDecelerateInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART

        addUpdateListener {
            val angleInDegree = String.format("%.6f", (it.animatedValue as Float)).toFloat()
            val angleInRadian = String.format("%.6f", toRadian(angleInDegree)).toFloat()
            val roundedAngle = angleInDegree.roundToInt()
            val percentageMoved = String.format("%.6f", getPercentageMoved(angleInDegree)).toFloat()
            END_X =
                (CENTER_X + (FIXED_RADIUS - RADIUS_PART) * percentageMoved * cos(angleInRadian))
            END_Y =
                (CENTER_Y + (FIXED_RADIUS - RADIUS_PART) * percentageMoved * sin(angleInRadian))

            handleSmallRectangle(roundedAngle, angleInRadian, percentageMoved)
            invalidate()
        }

        doOnRepeat {
            isExpanding = !isExpanding
        }
    }

    init {
//        valueAnimator.start()
    }

    private fun getPercentageMoved(angleInDegree: Float): Double {
        return if (isExpanding) {
            angleInDegree / 360.0
        } else {
            1 - (angleInDegree / 360.0)
        }
    }
    fun startAnimation(){
        valueAnimator.start()
    }

    private fun handleSmallRectangle(
        roundedAngle: Int,
        angleInRadian: Float,
        percentageMoved: Float,
    ) {
        val index = roundedAngle / 30
        if (boundaryPoints.getOrNull(index) == null) {
            val endPoint = EndPoint()
            setEndpoint(endPoint, percentageMoved, angleInRadian)
            boundaryPoints.add(endPoint)
        }

        boundaryPoints.forEach { point ->
            setEndpoint(point, percentageMoved, point.angleInRadian)
        }

    }

    private fun setEndpoint(endPoint: EndPoint, percentageMoved: Float, angle: Float) {
        endPoint.apply {
            angleInRadian = angle
            currStartX =
                (CENTER_X + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * cos(angle))
            currStartY =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * sin(angle))
            currEndX =
                (CENTER_X + (FIXED_RADIUS * percentageMoved) * cos(angle))
            currEndY =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved) * sin(angle))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        FIXED_RADIUS = (min(viewHeight, viewWidth) - 2 * PADDING) / 2
        RADIUS_PART = FIXED_RADIUS / 8
        CENTER_X = viewWidth / 2
        CENTER_Y = viewHeight / 2
        END_X = CENTER_X
        END_Y = CENTER_Y
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(CENTER_X, CENTER_Y, END_X, END_Y, paint)

        boundaryPoints.forEach { point ->
            canvas?.drawLine(
                point.currStartX,
                point.currStartY,
                point.currEndX,
                point.currEndY,
                paint
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        boundaryPoints.clear()
    }
}

data class EndPoint(
    var currStartX: Float = 0f,
    var currStartY: Float = 0f,
    var currEndX: Float = 0f,
    var currEndY: Float = 0f,
    var angleInRadian: Float = 0f,
)