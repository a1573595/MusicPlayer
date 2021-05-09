package com.a1573595.musicplayer.customView

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.*

class FloatingAnimationView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    lateinit var startPosition: Point
    lateinit var endPosition: Point

    private val random = Random()

    fun startAnimation() {
        val width = getScreenWidth()
        val height = getScreenHeight()

        val endPointRandom =
            Point(random.nextInt(width), endPosition.y)

        val bezierTypeEvaluator = BezierEvaluator(
            Point(random.nextInt(width), random.nextInt(height)),
            Point(random.nextInt(width / 2), random.nextInt(height / 2))
        )

        val animator = ValueAnimator.ofObject(bezierTypeEvaluator, startPosition, endPointRandom)

        animator.addUpdateListener { valueAnimator ->
            val point = valueAnimator.animatedValue as Point
            val fraction = valueAnimator.animatedFraction

            x = point.x.toFloat()
            y = point.y.toFloat()
            alpha = 1 - fraction

            invalidate()
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                val viewGroup = parent as ViewGroup
                viewGroup.removeView(this@FloatingAnimationView)
            }
        })

        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }
}