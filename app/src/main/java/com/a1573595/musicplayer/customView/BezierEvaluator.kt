package com.a1573595.musicplayer.customView

import android.animation.TypeEvaluator
import android.graphics.Point

class BezierEvaluator(private val controlPoint1: Point, private val controlPoint2: Point) : TypeEvaluator<Point> {
    override fun evaluate(t: Float, startValue: Point, endValue: Point): Point {
        val x = startValue.x * (1 - t) * (1 - t) * (1 - t) + (3f
                * controlPoint1.x * t * (1 - t) * (1 - t)) + (3f
                * controlPoint2.x * (1 - t) * t * t) + endValue.x * t * t *
                t
        val y = startValue.y * (1 - t) * (1 - t) * (1 - t) + (3f
                * controlPoint1.y * t * (1 - t) * (1 - t)) + (3f
                * controlPoint2.y * (1 - t) * t * t) + endValue.y * t * t *
                t
        return Point(x.toInt(), y.toInt())
    }
}