package com.bekado.bekadoonline.helper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.bekado.bekadoonline.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object Helper {
    fun addcoma3digit(number: Long?): String {
        val numberFormat: NumberFormat = DecimalFormat("#,###")
        return numberFormat.format(number)
    }

    fun delComa3digit(input: String?): Long {
        val stringWithoutDots = input!!.replace(".", "")
        return stringWithoutDots.toLong()
    }

    fun calcDistance(latUser: Double, lonUser: Double, latToko: Double, lonToko: Double): Double {
        val earth = 6371.0 // Radius bumi dalam kilometer
        val latRad1 = Math.toRadians(latUser)
        val lonRad1 = Math.toRadians(lonUser)
        val latRad2 = Math.toRadians(latToko)
        val lonRad2 = Math.toRadians(lonToko)

        val dLat = latRad2 - latRad1
        val dLon = lonRad2 - lonRad1

        val a = sin(dLat / 2).pow(2) + cos(latRad1) * cos(latRad2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earth * c
    }

    fun calculateSpanCount(requireContext: Context): Int {
        val displayMetrics = DisplayMetrics()
        (requireContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthPx = 150
        val spanCount = (screenWidth / columnWidthPx).toInt()

        return if (spanCount > 0) spanCount else 1
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showToastL(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun salinPesan(context: Context, textnya: CharSequence) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(textnya, textnya)
        clipboard.setPrimaryClip(clip)
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showAlertDialog(
        title: String,
        msg: String,
        positifBtn: String,
        context: Context,
        color: Int,
        positiveBtnClickListener: () -> Unit
    ) {
        val alertdialog = MaterialAlertDialogBuilder(context, R.style.alertDialog)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(true)
            .setNegativeButton(context.getString(R.string.batalkan)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(positifBtn) { _, _ ->
                positiveBtnClickListener.invoke()
            }.show()

        val negativeBtn = alertdialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val positiveBtn = alertdialog.getButton(DialogInterface.BUTTON_POSITIVE)

        negativeBtn.apply {
            textSize = 16f
            setTextColor(context.getColor(R.color.blue_grey_700))
        }

        positiveBtn.apply {
            textSize = 16f
            setTextColor(color)
        }
    }
}