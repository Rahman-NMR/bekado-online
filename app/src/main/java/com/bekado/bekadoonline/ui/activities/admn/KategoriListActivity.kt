package com.bekado.bekadoonline.ui.activities.admn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.admn.AdapterKategoriList
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.viewmodel.KategoriListViewModel
import com.bekado.bekadoonline.databinding.ActivityKategoriListBinding
import com.bekado.bekadoonline.helper.Helper.hideKeyboard
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.ui.activities.admn.ProdukListActivity.Companion.dataKategoriModel
import com.bekado.bekadoonline.ui.bottomsheet.admn.BottomSheetEditKategori
import com.google.firebase.database.FirebaseDatabase

class KategoriListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKategoriListBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var adapterKategoriList: AdapterKategoriList
    private lateinit var kategoriListViewModel: KategoriListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKategoriListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        kategoriListViewModel = ViewModelProvider(this)[KategoriListViewModel::class.java]

        setupAdapter()
        setupKategoriList()

        with(binding) {
            rvKategori.layoutManager = LinearLayoutManager(this@KategoriListActivity, LinearLayoutManager.VERTICAL, false)
            appBar.setNavigationOnClickListener { finish() }
            etAddKategori.addTextChangedListener(addKategoriTextWatcher)
        }
    }

    private fun setupKategoriList() {
        kategoriListViewModel.kategoriList.observe(this) { kategoriList ->
            adapterKategoriList.submitList(kategoriList)

            with(binding) {
                val visibiliti = if (kategoriList != null) View.VISIBLE else View.GONE
                rvKategori.visibility = visibiliti
                lineDivider.visibility = visibiliti

                fabAddKategori.setOnClickListener { validateEditText(kategoriList) }
                etAddKategori.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        validateEditText(kategoriList)
                        true
                    } else false
                }
                etAddKategori.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        etAddKategori.clearFocus()
                        etAddKategori.error = null
                        true
                    } else false
                }
            }
        }
        kategoriListViewModel.isLoading.observe(this) { isLoading ->
            binding.progressbarKategoriList.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAdapter() {
        adapterKategoriList = AdapterKategoriList({ kategori ->
            dataKategoriModel = kategori
            startActivity(Intent(this@KategoriListActivity, ProdukListActivity::class.java))
        }, { kategori ->
            val ref = db.getReference("produk/kategori/${kategori.idKategori}")
            BottomSheetEditKategori(this@KategoriListActivity).showDialog(ref, kategori)
        })

        binding.rvKategori.adapter = adapterKategoriList
    }

    private fun validateEditText(kategoriList: ArrayList<KategoriModel>?) {
        if (isConnected(this@KategoriListActivity)) if (binding.etAddKategori.error == null) {
            val errorMsg = "${getString(R.string.nama_kategori)} ${getString(R.string.tidak_dapat_kosong)}"

            if (binding.etAddKategori.text.isNotEmpty()) addKategoriToDatabase(binding.etAddKategori, kategoriList)
            else showToast(errorMsg, this@KategoriListActivity)
        }
    }

    private fun addKategoriToDatabase(etAddKategori: EditText, kategoriList: ArrayList<KategoriModel>?) {
        val kategoriRef = db.getReference("produk/kategori")
        val addKategori = HashMap<String, Any>()
        val idKategori = kategoriRef.push().key.toString()
        addKategori["idKategori"] = idKategori
        addKategori["namaKategori"] = etAddKategori.text.toString().trim()
        addKategori["posisi"] = (kategoriList?.size ?: 0) + 1
        addKategori["visibilitas"] = false
        kategoriRef.child(idKategori).setValue(addKategori)
            .addOnFailureListener { showToast("Terjadi kesalahan: ${it.message}", this) }

        etAddKategori.clearFocus()
        etAddKategori.text.clear()
        etAddKategori.error = null
        hideKeyboard(this, etAddKategori)
    }

    private val addKategoriTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val textInput = binding.etAddKategori.text.toString().trim { it <= ' ' }
            if (textInput.isNotEmpty()) binding.fabAddKategori.visibility = View.VISIBLE
            else binding.fabAddKategori.visibility = View.GONE
        }

        override fun afterTextChanged(s: Editable?) {
            val textInput = binding.etAddKategori.text
            val errorMsg = "${getString(R.string.nama_kategori)} ${getString(R.string.tidak_dapat_kosong)}"

            if (s == textInput) {
                if (textInput.toString().trim().isEmpty()) binding.etAddKategori.error = errorMsg
                else binding.etAddKategori.error = null
            }
        }
    }
}