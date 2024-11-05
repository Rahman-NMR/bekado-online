package com.bekado.bekadoonline.view.ui.admn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.databinding.ActivityKategoriListBinding
import com.bekado.bekadoonline.helper.Helper.hideKeyboard
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_KATEGORI
import com.bekado.bekadoonline.view.adapter.admn.AdapterKategoriList
import com.bekado.bekadoonline.view.ui.bottomsheet.admn.BottomSheetEditKategori
import com.bekado.bekadoonline.view.viewmodel.admin.AdminViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.admin.KategoriListViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

class KategoriListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKategoriListBinding
    private lateinit var adapterKategoriList: AdapterKategoriList

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val kategoriListViewModel: KategoriListViewModel by viewModels { AdminViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKategoriListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        dataAkunHandler()
        setupAdapter()
        kategoriListHandler()

        with(binding) {
            rvKategori.layoutManager = LinearLayoutManager(this@KategoriListActivity, LinearLayoutManager.VERTICAL, false)
            appBar.setNavigationOnClickListener { finish() }
            etAddKategori.addTextChangedListener(addKategoriTextWatcher)
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            akun?.let { if (!it.statusAdmin) finish() } ?: finish()
        }
    }

    private fun kategoriListHandler() {
        kategoriListViewModel.getKategoriList().observe(this) { kategoriList ->
            adapterKategoriList.submitList(kategoriList)

            with(binding) {
                rvKategori.isVisible = kategoriList != null
                lineDivider.isVisible = kategoriList != null

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
        kategoriListViewModel.isLoading().observe(this) { isLoading ->
            binding.progressbarKategoriList.isVisible = isLoading
        }
    }

    private fun setupAdapter() {
        adapterKategoriList = AdapterKategoriList({ kategori ->
            val mIntent = Intent(this@KategoriListActivity, ProdukListActivity::class.java)
                .putExtra(EXTRA_ID_KATEGORI, kategori.idKategori)
            startActivity(mIntent)
        }, { kategori ->
            BottomSheetEditKategori(this@KategoriListActivity, kategori, kategoriListViewModel).showDialog()
        })

        binding.rvKategori.adapter = adapterKategoriList
    }

    private fun validateEditText(kategoriList: ArrayList<KategoriModel>?) {
        if (isConnected(this@KategoriListActivity)) if (binding.etAddKategori.error == null) {
            if (binding.etAddKategori.text.isNotEmpty()) addKategoriToDatabase(binding.etAddKategori, kategoriList)
            else showToast(getString(R.string.tidak_dapat_kosong, getString(R.string.nama_kategori)), this@KategoriListActivity)
        }
    }

    private fun addKategoriToDatabase(etAddKategori: EditText, kategoriList: ArrayList<KategoriModel>?) {
        kategoriListViewModel.addKategori(etAddKategori.text.toString().trim(), ((kategoriList?.size ?: 0) + 1).toLong()) { isSuccessful ->
            if (isSuccessful) {
                etAddKategori.clearFocus()
                etAddKategori.text.clear()
                etAddKategori.error = null
                hideKeyboard(this, etAddKategori)
            } else showToast(getString(R.string.gagal_menambahkan_x_baru, getString(R.string.kategori)), this)
        }
    }

    private val addKategoriTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val textInput = binding.etAddKategori.text.toString().trim { it <= ' ' }
            binding.fabAddKategori.isVisible = textInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val textInput = binding.etAddKategori.text

            if (s == textInput) {
                if (textInput.toString().trim().isEmpty()) binding.etAddKategori.error =
                    getString(R.string.tidak_dapat_kosong, getString(R.string.nama_kategori))
                else binding.etAddKategori.error = null
            }
        }
    }
}