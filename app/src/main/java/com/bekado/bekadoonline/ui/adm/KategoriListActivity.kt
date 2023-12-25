package com.bekado.bekadoonline.ui.adm

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.admn.AdapterKategoriList
import com.bekado.bekadoonline.bottomsheet.admn.ShowEditKategoriBottomSheet
import com.bekado.bekadoonline.databinding.ActivityKategoriListBinding
import com.bekado.bekadoonline.helper.Helper.hideKeyboard
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.helper.ItemMoveCallback
import com.bekado.bekadoonline.model.KategoriModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KategoriListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKategoriListBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var adapterKategoriList: AdapterKategoriList
    private var dataKategori: ArrayList<KategoriModel> = ArrayList()

    private lateinit var kategoriRef: DatabaseReference
    private lateinit var kategoriListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKategoriListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        kategoriRef = db.getReference("produk")

        getKategoriList()

        with(binding) {
            rvKategori.layoutManager = LinearLayoutManager(this@KategoriListActivity, LinearLayoutManager.VERTICAL, false)
            appBar.setNavigationOnClickListener { onBackPressed() }

            fabAddKategori.setOnClickListener { validateEditText() }
            etAddKategori.addTextChangedListener(addKategoriTextWatcher)
            etAddKategori.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateEditText()
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
            btnPerbaruiPosisi.setOnClickListener { if (isConnected(this@KategoriListActivity)) setupUbahPosisi() }
        }
    }

    private fun getKategoriList() {
        kategoriListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataKategori.clear()

                for (item in snapshot.child("kategori").children) {
                    val idKategori = item.child("idKategori").value as String
                    val namaKategori = item.child("namaKategori").value as String
                    val posisi = item.child("posisi").value as Long
                    val visibilitas = item.child("visibilitas").value as Boolean

                    var jumlahProdukNya = 0
                    for (data in snapshot.child("produk").children) {
                        val jumlahProduk = data.child("idKategori").value.toString()
                        if (jumlahProduk == idKategori) jumlahProdukNya++
                    }
                    val kategoriData = KategoriModel(idKategori, namaKategori, posisi, visibilitas, jumlahProdukNya.toString())
                    dataKategori.add(kategoriData)
                    dataKategori.sortBy { it.posisi }
                    binding.lineDivider.visibility = if (dataKategori.isEmpty()) View.GONE else View.VISIBLE
                }
                adapterKategoriList = AdapterKategoriList(dataKategori, { kategori ->
                    val intents = Intent(this@KategoriListActivity, ProdukListActivity::class.java)
                        .putExtra("idKategori", kategori)
                    startActivity(intents)
                }, { kategori ->
                    val ref = kategoriRef.child("kategori/${kategori.idKategori}")
                    ShowEditKategoriBottomSheet(this@KategoriListActivity).showDialog(
                        this@KategoriListActivity, kategori.namaKategori, kategori.visibilitas, ref
                    )
                })
                val itemTouchHelperCallback = ItemMoveCallback(adapterKategoriList, binding)
                val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
                itemTouchHelper.attachToRecyclerView(binding.rvKategori)

                binding.rvKategori.adapter = adapterKategoriList
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        kategoriRef.addValueEventListener(kategoriListener)
    }

    private fun ActivityKategoriListBinding.validateEditText() {
        if (isConnected(this@KategoriListActivity)) if (etAddKategori.error == null)
            if (etAddKategori.text.isNotEmpty()) addKategoriToDatabase(etAddKategori)
            else showToast("${getString(R.string.nama_kategori)} ${getString(R.string.tidak_dapat_kosong)}", this@KategoriListActivity)
    }

    private fun addKategoriToDatabase(etAddKategori: EditText) {
        val kategoriRef = db.getReference("produk/kategori")
        val addKategori = HashMap<String, Any>()
        val idKategori = kategoriRef.push().key.toString()
        addKategori["idKategori"] = idKategori
        addKategori["namaKategori"] = etAddKategori.text.toString().trim()
        addKategori["posisi"] = dataKategori.size + 1
        addKategori["visibilitas"] = false
        kategoriRef.child(idKategori).setValue(addKategori)
            .addOnSuccessListener { getKategoriList() }
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

    private fun setupUbahPosisi() {
        val kategoriRef = db.getReference("produk/kategori")
        val updates = HashMap<String, Any>()

        for (i in dataKategori.indices) {
            val kategori = dataKategori[i]
            val kategoriId = kategori.idKategori
            updates["$kategoriId/posisi"] = kategori.posisi
        }
        kategoriRef.updateChildren(updates)
            .addOnSuccessListener { binding.btnPerbaruiPosisi.visibility = View.GONE }
    }

    override fun onDestroy() {
        super.onDestroy()
        kategoriRef.removeEventListener(kategoriListener)
    }
}