package com.bekado.bekadoonline.bottomsheet.admn

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.BottomsheetEditKategoriBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference

class ShowEditKategoriBottomSheet(context: Context) {
    private var bindingBS: BottomsheetEditKategoriBinding
    private var dialog: BottomSheetDialog

    init {
        bindingBS = BottomsheetEditKategoriBinding.inflate(LayoutInflater.from(context))
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(context: Context, namaKategori: String?, visibilitas: Boolean, ref: DatabaseReference, jumlahProduk: Long) {
        with(bindingBS) {
            val toastTxt = "${context.getString(R.string.produk)} ${context.getString(R.string.tidak_dapat_kosong)}"

            title.text = context.getString(R.string.edit_kategori)
            namaKategoriView.text = namaKategori
            namaKategoriEdit.setText(namaKategori)
            kategoriVisibilitas.isChecked = visibilitas
            kategoriVisibilitas.setOnCheckedChangeListener { _, checked ->
                if (jumlahProduk.toInt() > 0) Handler().postDelayed({ setVisibility(context, ref, checked, namaKategori) }, 500)
                else showToast(toastTxt, context)
            }

            btnEditNamaKategori.setOnClickListener {
                toggleEditView(namaKategoriEdit, namaKategoriView, btnEditNamaKategori, btnCancelNamaKategori)
                if (!namaKategoriEdit.isEnabled) {
                    if (HelperConnection.isConnected(context)) if (namaKategori != namaKategoriEdit.text.toString()) {
                        updateData(context, context.getString(R.string.nama_kategori), namaKategoriEdit, ref, namaKategoriEdit.text.toString().trim())
                        dialog.cancel()
                    }
                } else focusRequest(context, namaKategoriEdit)
            }
            btnCancelNamaKategori.setOnClickListener {
                toggleEditView(namaKategoriEdit, namaKategoriView, btnEditNamaKategori, btnCancelNamaKategori)
            }
        }

        dialog.show()
    }

    private fun setVisibility(context: Context, ref: DatabaseReference, isChecked: Boolean, namaKategori: String?) {
        val visibilitas = if (isChecked) "ditampilkan" else "disembunyikan"

        if (HelperConnection.isConnected(context))
            ref.child("visibilitas").setValue(isChecked).addOnSuccessListener {
                showToast("Kategori $namaKategori $visibilitas", context)
                dialog.cancel()
            }
    }

    private fun updateData(context: Context, string: String, editText: EditText, ref: DatabaseReference, value: String) {
        if (editText.text.isNotEmpty()) {
            ref.child("namaKategori").setValue(value)
                .addOnSuccessListener { showToast("$string ${context.getString(R.string.berhasil_mengubah)}", context) }
                .addOnFailureListener { showToast("$string ${context.getString(R.string.gagal_mengubah)}", context) }
        } else showToast("$string ${context.getString(R.string.tidak_dapat_kosong)}", context)
    }

    private fun toggleEditView(
        editText: EditText,
        textView: TextView,
        editButton: MaterialButton,
        cancelButton: MaterialButton
    ) {
        editText.isEnabled = !editText.isEnabled
        editText.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
        textView.visibility = if (!editText.isEnabled) View.VISIBLE else View.GONE
        editButton.setIconResource(if (editText.isEnabled) R.drawable.icon_round_done_24 else R.drawable.icon_round_mode_edit_24)
        cancelButton.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
    }

    private fun focusRequest(context: Context, editText: EditText) {
        editText.requestFocus()

        editText.setSelection(editText.text.length)
        val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}