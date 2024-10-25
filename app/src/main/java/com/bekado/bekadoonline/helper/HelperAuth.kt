package com.bekado.bekadoonline.helper

import android.content.Context
import android.view.MenuItem
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.helper.Helper.showToast

object HelperAuth {
    fun adminKeranjangState(context: Context, item: MenuItem) {
        when (item.itemId) {
            R.id.menu_keranjang -> showToast(context.getString(R.string.restricted_admin), context)
        }
    }
}