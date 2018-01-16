package com.varvet.voicetotextarch

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.animation.ObjectAnimator
import android.animation.Animator
import android.animation.AnimatorSet
import android.support.v4.content.ContextCompat
import android.util.Property
import android.view.View
import java.util.concurrent.ThreadLocalRandom

class RippleView(context: Context?, private val attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val DEFAULT_RIPPLE_SCALE = 4f
    private val MIN_RIPPLE_DURATION = 400
    private val MAX_RIPPLE_DURATION = 1000

    companion object {
        /**
         * This enum is tightly coupled with attrs#RippleView. This should be redesign if possible.
         */
        enum class FillStyle(val type: Int) {
            FILL(type = 0), STROKE(type = 1), FILL_AND_STROKE(type = 2)
        }
    }

    private var rippleScale: Float = DEFAULT_RIPPLE_SCALE
    private var rippleColor: Int = 0
    private var rippleType: Int = 0
    private var rippleStrokeWidth: Float = 0f
    private var rippleRadius: Float = 0f

    init {
        if (context == null) throw IllegalArgumentException("Context is null.")
        if (attrs == null) throw IllegalArgumentException("Attribute set is null.")

        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.RippleView)
        rippleRadius = styledAttributes.getDimension(R.styleable.RippleView_rv_radius, resources.getDimension(R.dimen.rippleRadius))
        rippleStrokeWidth = styledAttributes.getDimension(R.styleable.RippleView_rv_strokeWidth, resources.getDimension(R.dimen.rippleStrokeWidth))
        rippleColor = styledAttributes.getColor(R.styleable.RippleView_rv_color, ContextCompat.getColor(context, R.color.colorAccent))
        rippleType = styledAttributes.getInt(R.styleable.RippleView_rv_type, FillStyle.FILL.type)
        rippleScale = styledAttributes.getFloat(R.styleable.RippleView_rv_scale, DEFAULT_RIPPLE_SCALE)
        styledAttributes.recycle()
    }

    /**
     * Call this class to initiate a new ripple animation.
     */
    fun newRipple() {
        val circleView = CircleView(context, attrs, rippleColor, rippleType, rippleStrokeWidth).apply {
            visibility = View.VISIBLE
        }

        addView(circleView, getCircleViewLayoutParams())

        generateRipple(duration = randomAnimationDuration(), target = circleView).apply {
            start()
        }
    }

    private fun getCircleViewLayoutParams(): LayoutParams {
        val widthHeight = (2 * (rippleRadius + rippleStrokeWidth)).toInt()
        return RelativeLayout.LayoutParams(widthHeight, widthHeight).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }
    }

    private fun randomAnimationDuration() = ThreadLocalRandom.current().nextInt(MIN_RIPPLE_DURATION, MAX_RIPPLE_DURATION)

    private fun generateRipple(duration: Int, target: CircleView): AnimatorSet {
        val animatorList = ArrayList<Animator>()

        animatorList.add(provideAnimator(
                target = target,
                type = View.SCALE_X,
                animDuration = duration,
                scale = rippleScale
        ))

        animatorList.add(provideAnimator(
                target = target,
                type = View.SCALE_Y,
                animDuration = duration,
                scale = rippleScale
        ))

        animatorList.add(provideAnimator(
                target = target,
                type = View.ALPHA,
                animDuration = duration
        ))

        return AnimatorSet().apply {
            playTogether(animatorList)
        }
    }

    private fun provideAnimator(
            target: View,
            type: Property<View, Float>,
            animDuration: Int,
            scale: Float = DEFAULT_RIPPLE_SCALE
    ): ObjectAnimator {
        val scaleAmount = if (type == View.ALPHA) 0f else scale

        return ObjectAnimator.ofFloat(target, type, 1.0f, scaleAmount).apply {
            duration = animDuration.toLong()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    removeView(target)
                }
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
            })
        }
    }
}