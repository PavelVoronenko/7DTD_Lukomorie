package com.antago30.a7dtd_lukomorie.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.antago30.a7dtd_lukomorie.R
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

                    if (e1 == null || e2 == null) return false

                    val deltaX = e2.x - e1.x
                    val deltaY = e2.y - e1.y

                    // Найдём drawer-меню ПРЯМО СЕЙЧАС (в момент свайпа)
                    val drawerView = findViewById<View>(R.id.custom_drawer_menu)
                    if (drawerView == null) return false // защита от NPE

                    // Свайп вправо → открыть меню
                    if (abs(deltaX) > abs(deltaY) && deltaX > 50 && velocityX > 0) {
                        if (!isDrawerOpen(drawerView)) {
                            openDrawer(drawerView)
                            return true
                        }
                    }

                    // Свайп влево → закрыть меню
                    if (abs(deltaX) > abs(deltaY) && deltaX < -50 && velocityX < 0) {
                        if (isDrawerOpen(drawerView)) {
                            closeDrawer(drawerView)
                            return true
                        }
                    }

                    return false
                }
            }
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }
}