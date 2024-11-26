package com.bonak.steady

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import org.osmdroid.views.MapView

class CustomMapView : MapView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Request all parents to not intercept touch events
        parent?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}
