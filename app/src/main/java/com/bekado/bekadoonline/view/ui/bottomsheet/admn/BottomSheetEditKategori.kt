package com.bekado.bekadoonline.view.ui.bottomsheet.admn

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.databinding.BottomsheetEditKategoriBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.viewmodel.admin.KategoriListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class BottomSheetEditKategori(
    private val context: Context,
    private val kategori: KategoriModel,
    private val kategoriListViewModel: KategoriListViewModel
) {
    private var bindingBS: BottomsheetEditKategoriBinding = BottomsheetEditKategoriBinding.inflate(LayoutInflater.from(context))
    private var dialog: BottomSheetDialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)

    init {
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog() {
        with(bindingBS) {
            val namaKategori = kategori.namaKategori
            val idKategori = kategori.idKategori
            val jumlahProduk = kategori.jumlahProduk.toInt()
            val produkHidden = kategori.produkHidden.toInt()

            title.text = context.getString(R.string.edit_kategori)
            namaKategoriView.text = namaKategori
            namaKategoriEdit.setText(namaKategori)
            kategoriVisibilitas.isChecked = kategori.visibilitas
            kategoriVisibilitas.setOnCheckedChangeListener { _, checked ->
                if (jumlahProduk > 0 && produkHidden < jumlahProduk) {
                    Handler(Looper.getMainLooper()).postDelayed({ setVisibility(checked, namaKategori, idKategori) }, 300)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ showToast(context.getString(R.string.msg_beranda_kosong), context) }, 300)
                    dialog.cancel()
                }
            }

            btnEditNamaKategori.setOnClickListener {
                toggleEditView(namaKategoriEdit, namaKategoriView, btnEditNamaKategori, btnCancelNamaKategori)
                if (!namaKategoriEdit.isEnabled) {
                    if (HelperConnection.isConnected(context)) if (namaKategori != namaKategoriEdit.text.toString()) {
                        setNamaKategori(context.getString(R.string.nama_kategori), namaKategoriEdit.text, idKategori)
                        dialog.cancel()
                    }
                } else focusRequest(namaKategoriEdit)
            }
            btnCancelNamaKategori.setOnClickListener {
                toggleEditView(namaKategoriEdit, namaKategoriView, btnEditNamaKategori, btnCancelNamaKategori)
            }
        }

        dialog.show()
    }

    private fun setVisibility(isChecked: Boolean, namaKategori: String?, idKategori: String?) {
        val visibilitas = if (isChecked) "ditampilkan" else "disembunyikan"

        if (HelperConnection.isConnected(context))
            kategoriListViewModel.updateVisibilitasKategori(idKategori, isChecked) { isSuccessful ->
                if (isSuccessful) {
                    showToast("Kategori $namaKategori $visibilitas", context)
                    dialog.cancel()
                } else showToast(context.getString(R.string.tidak_dapat_menampilkan_x, namaKategori), context)
            }
    }

    private fun setNamaKategori(namaKategori: String, editText: Editable, idKategori: String?) {
        if (editText.isNotEmpty()) {
            kategoriListViewModel.updateNamaKategori(idKategori, editText.toString().trim()) { isSucessful ->
                if (isSucessful) showToast(context.getString(R.string.berhasil_diperbarui, namaKategori), context)
                else showToast("$namaKategori ${context.getString(R.string.gagal_mengubah)}", context)
            }
        } else showToast(context.getString(R.string.tidak_dapat_kosong, namaKategori), context)
    }

    private fun toggleEditView(
        editText: EditText,
        textView: TextView,
        editButton: MaterialButton,
        cancelButton: MaterialButton
    ) {
        editText.isEnabled = !editText.isEnabled
        editText.isVisible = editText.isEnabled
        textView.isVisible = !editText.isEnabled
        editButton.setIconResource(if (editText.isEnabled) R.drawable.icon_round_done_24 else R.drawable.icon_round_mode_edit_24)
        cancelButton.isVisible = editText.isEnabled
    }

    private fun focusRequest(editText: EditText) {
        editText.requestFocus()

        editText.setSelection(editText.text.length)
        val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}