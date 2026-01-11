package com.example.android.medicinecabinet.utils

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup

object Functions {
    fun Int.dp() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun View.setMarginTop(marginDp: Int){
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = marginDp.dp()
        layoutParams = lp
    }

    fun View.setMarginBottom(marginDp: Int){
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = marginDp.dp()
        layoutParams = lp
    }

    fun View.setMarginStart(marginDp: Int){
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.marginStart = marginDp.dp()
        layoutParams = lp
    }

    fun View.setMarginEnd(marginDp: Int){
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.marginEnd = marginDp.dp()
        layoutParams = lp
    }
}