package com.bekado.bekadoonline.helper

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import java.text.DecimalFormat
import java.text.NumberFormat

object Helper {
    fun addcoma3digit(number: Long?): String {
        val numberFormat: NumberFormat = DecimalFormat("#,###")
        return numberFormat.format(number)
    }

    fun delComa3digit(input: String?): Long {
        val stringWithoutDots = input!!.replace(".", "")
        return stringWithoutDots.toLong()
    }

    fun calculateSpanCount(requireContext: Context): Int {
        val displayMetrics = DisplayMetrics()
        (requireContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthPx = 150
        val spanCount = (screenWidth / columnWidthPx).toInt()

        return if (spanCount > 0) spanCount else 1
    }
}