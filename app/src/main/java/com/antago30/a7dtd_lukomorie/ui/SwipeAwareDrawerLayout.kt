package com.antago30.a7dtd_lukomorie.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlin.math.abs

class SwipeAwareDrawerLayout : DrawerLayout {

    private lateinit var gestureDetector: GestureDetector

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    Log.d("SwipeAwareDrawer", "onFling called!")

                    if (e1 != null && e2 != null) {
                        val deltaX = e2.x - e1.x
                        val deltaY = e2.y - e1.y

                        if (abs(deltaX) > abs(deltaY) && deltaX > 10) {
                            Log.d("SwipeAwareDrawer", "SWIPE LEFT TO RIGHT detected! deltaX=$deltaX")
                            openDrawer(GravityCompat.START)
                            return true
                        }
                    }
                    return false
                }
            }
        )
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }
}