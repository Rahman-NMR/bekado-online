package com.bekado.bekadoonline.helper

import android.content.Context
import android.content.DialogInterface
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.bekado.bekadoonline.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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