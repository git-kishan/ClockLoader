package com.example.loadertest

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnRepeat
import kotlin.math.*

class Loader @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : View(context, attrs) {


    private var viewHeight: Float = 0f
    private var viewWidth: Float = 0f
    private var FIXED_RADIUS = 0f
    private var radius: Float = 0f
    private var RADIUS_PART = 0f
    private val PADDING = 50f
    private val WIDTH = 20f
    private var CENTER_X = 0f
    private var CENTER_Y = 0f
    private var END_X = 0f
    private var END_Y = 0f
    private var shouldDrawRectangle = false
    private var boundaryPoints = mutableListOf<EndPoints>()

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        strokeJoin = Paint.Join.BEVEL
        strokeWidth = WIDTH
    }

    private fun toRadian(angleInDegree: Float): Double {
        return angleInDegree * (PI / 180f)
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 4000
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART

        addUpdateListener {
            val angleInDegree = it.animatedValue as Float
            val angleInRadian = toRadian(angleInDegree)
            END_X = CENTER_X + radius * cos(angleInRadian).toFloat()
            END_Y = CENTER_Y + radius * sin(angleInRadian).toFloat()
            val roundedAngle = angleInDegree.roundToInt()
            handleSmallRectangle(angleInDegree, roundedAngle, angleInRadian)
            invalidate()
        }

        doOnRepeat {
            radius = FIXED_RADIUS
            boundaryPoints.clear()
        }
    }


    init {
        valueAnimator.start()
    }

    private fun handleSmallRectangle(
        angleInFloat: Float,
        roundedAngle: Int,
        angleInRadian: Double
    ) {
        val percentageMoved = 1 - angleInFloat / 360.0

        boundaryPoints.forEach { point ->
            point.currStartX =
                (CENTER_X + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * cos(point.angleInRadian)).toFloat()
            point.currStartY =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * sin(point.angleInRadian)).toFloat()
            point.currEndX =
                (CENTER_X + (FIXED_RADIUS * percentageMoved) * cos(point.angleInRadian)).toFloat()
            point.currEndY =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved) * sin(point.angleInRadian)).toFloat()
        }

        if (roundedAngle % 30 == 0) {
            radius -= RADIUS_PART
            shouldDrawRectangle = true

            val CURRENT_START_X =
                (CENTER_X + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * cos(angleInRadian)).toFloat()

            val CURRENT_START_Y =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved - RADIUS_PART) * sin(angleInRadian)).toFloat()

            val CURRENT_END_X =
                (CENTER_X + (FIXED_RADIUS * percentageMoved) * cos(angleInRadian)).toFloat()

            val CURRENT_END_Y =
                (CENTER_Y + (FIXED_RADIUS * percentageMoved) * sin(angleInRadian)).toFloat()

            val index = roundedAngle / 30

            if (boundaryPoints.getOrNull(index) == null) {
                boundaryPoints.add(
                    EndPoints(
                        CURRENT_START_X,
                        CURRENT_START_Y,
                        CURRENT_END_X,
                        CURRENT_END_Y,
                        angleInRadian
                    )
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        FIXED_RADIUS = (min(viewHeight, viewWidth) - 2 * PADDING) / 2
        radius = FIXED_RADIUS
        RADIUS_PART = radius / 12
        CENTER_X = viewWidth / 2
        CENTER_Y = viewHeight / 2
        END_X = CENTER_X + radius
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
}

data class EndPoints(
    var currStartX: Float,
    var currStartY: Float,
    var currEndX: Float,
    var currEndY: Float,
    var angleInRadian: Double,
)