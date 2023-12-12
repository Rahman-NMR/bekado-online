package com.bekado.bekadoonline.ui

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.LoginActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterKeranjang
import com.bekado.bekadoonline.databinding.ActivityKeranjangBinding
import com.bekado.bekadoonline.helper.GridSpacingItemDecoration
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperProduk
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bekado.bekadoonline.model.ProdukModel
import com.example.testnew.model.KeranjangModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeranjangBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapterKeranjang: AdapterKeranjang
    private var dataKeranjang: ArrayList<CombinedKeranjangModel> = ArrayList()
    private lateinit var adapterKeranjangHide: AdapterKeranjang
    private var dataKeranjangHide: ArrayList<CombinedKeranjangModel> = ArrayList()

    private lateinit var alamatRef: DatabaseReference
    private lateinit var alamatListener: ValueEventListener
    private lateinit var keranjangRef: DatabaseReference
    private lateinit var produkRef: DatabaseReference
    private lateinit var kategoriRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        alamatRef = db.getReference("alamat/${auth.currentUser?.uid}")
        keranjangRef = db.getReference("keranjang/${auth.currentUser?.uid}")
        produkRef = db.getReference("produk/produk")
        kategoriRef = db.getReference("produk/kategori")

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }
            loadingIndicator.visibility = View.VISIBLE

            val lmActive = LinearLayoutManager(this@KeranjangActivity, LinearLayoutManager.VERTICAL, false)
            val lmNonActive = LinearLayoutManager(this@KeranjangActivity, LinearLayoutManager.VERTICAL, false)
            val padding = resources.getDimensionPixelSize(R.dimen.smalldp)

            rvDaftarPesanan.layoutManager = lmActive
            rvDaftarPesanan.addItemDecoration(GridSpacingItemDecoration(1, padding, false))
            rvDaftarPesananHide.layoutManager = lmNonActive
            rvDaftarPesananHide.addItemDecoration(GridSpacingItemDecoration(1, padding, false))

            if (auth.currentUser != null) {
                getDataKeranjang()
//            getAlamatPenerima()

//            binding.llPengirim.setOnClickListener { startActivity(Intent(activity, UbahAlamatActivity::class.java)) }
            }
        }
    }

    private fun getDataKeranjang() {
        keranjangRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(keranjangSnapshot: DataSnapshot) {
                dataKeranjang.clear()
                dataKeranjangHide.clear()

                if (keranjangSnapshot.exists()) {
                    for (keranjangList in keranjangSnapshot.children) {
                        val idKeranjang = keranjangList.child("idProduk").value as String

                        produkRef.child(idKeranjang).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(produkSnapshot: DataSnapshot) {
                                val idKategori = produkSnapshot.child("idKategori").value as String
                                val visibility = produkSnapshot.child("visibility").value as Boolean

                                kategoriRef.child(idKategori).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(kategoriSnapshot: DataSnapshot) {
                                        val visibilitas = kategoriSnapshot.child("visibilitas").value as Boolean

                                        handleProductData(produkSnapshot, keranjangList, visibility, visibilitas, idKeranjang)
                                        setAdapter()
                                        binding.loadingIndicator.visibility = View.GONE
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                } else {
                    with(binding) {
//                        keranjangKosong.visibility = View.VISIBLE

                        rvDaftarPesanan.visibility = View.GONE
                        llProdukNoProses.visibility = View.GONE
                        rvDaftarPesananHide.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                with(binding) {
//                    keranjangKosong.visibility = View.GONE

                    rvDaftarPesanan.visibility = View.GONE
                    llProdukNoProses.visibility = View.GONE
                    rvDaftarPesananHide.visibility = View.GONE
                }
            }
        })
    }

    private fun handleProductData(
        produkSnapshot: DataSnapshot,
        keranjangList: DataSnapshot,
        visibility: Boolean,
        visibilitas: Boolean,
        idKeranjang: String
    ) {
        val itemToRemove = dataKeranjang.find { it.produkModel?.idProduk == idKeranjang }
        val itemToRemoveHide = dataKeranjangHide.find { it.produkModel?.idProduk == idKeranjang }

        if (itemToRemove != null && itemToRemove.produkModel?.visibility != visibility ||
            itemToRemove != null && !visibilitas
        ) dataKeranjang.remove(itemToRemove)

        if (itemToRemoveHide != null && itemToRemoveHide.produkModel?.visibility != visibility ||
            itemToRemove != null && visibilitas
        ) dataKeranjangHide.remove(itemToRemoveHide)

        if (visibilitas && visibility) {
            val produkData = produkSnapshot.getValue(ProdukModel::class.java)
            val keranjangData = keranjangList.getValue(KeranjangModel::class.java)

            val isProductExists = dataKeranjang.any { it.produkModel?.idProduk == produkData?.idProduk }
            if (!isProductExists) dataKeranjang.add(CombinedKeranjangModel(produkData, keranjangData))
            else {
                val existingItem = dataKeranjang.find { it.produkModel?.idProduk == produkData?.idProduk }
                existingItem?.produkModel = produkData
            }

        } else if (!visibilitas || !visibility) {
            val produkData = produkSnapshot.getValue(ProdukModel::class.java)

            val isProductExists = dataKeranjangHide.any { it.produkModel?.idProduk == produkData?.idProduk }
            if (!isProductExists) dataKeranjangHide.add(CombinedKeranjangModel(produkData, null))
            else {
                val existingItem = dataKeranjangHide.find { it.produkModel?.idProduk == produkData?.idProduk }
                existingItem?.produkModel = produkData
            }
        }
    }

    private fun setAdapter() {
        updateTotalHarga()

        adapterKeranjang = AdapterKeranjang(dataKeranjang, { keranjang, isChecked ->
            keranjang.keranjangModel?.diPilih = isChecked
            keranjangRef.child("${keranjang.produkModel?.idProduk}/diPilih").setValue(isChecked)
        }, { keranjang ->
            if (HelperConnection.isConnected(this))
                HelperProduk.deleteKeranjang(
                    keranjang,
                    keranjangRef.child("${keranjang.produkModel?.idProduk}"),
                    binding.root,
                    binding.llContainerPesanan,
                    true,
                    adapterKeranjang.notifyDataSetChanged()
                )
            binding.llProdukSelected.visibility = View.GONE
        })
        adapterKeranjangHide = AdapterKeranjang(dataKeranjangHide, { _, _ ->
        }, { keranjang ->
            if (HelperConnection.isConnected(this))
                HelperProduk.deleteKeranjang(
                    keranjang,
                    keranjangRef.child("${keranjang.produkModel?.idProduk}"),
                    binding.root,
                    binding.llContainerPesanan,
                    false,
                    adapterKeranjang.notifyDataSetChanged()
                )
        })

        with(binding) {
            rvDaftarPesanan.adapter = adapterKeranjang
            rvDaftarPesananHide.adapter = adapterKeranjangHide

//            keranjangKosong.visibility = if (adapterKeranjang.itemCount == 0 && adapterKeranjangHide.itemCount == 0) View.VISIBLE else View.GONE
            llProdukNoProses.visibility = if (adapterKeranjangHide.itemCount >= 1) View.VISIBLE else View.GONE
            rvDaftarPesanan.visibility = if (adapterKeranjang.itemCount == 0) View.GONE else View.VISIBLE
            rvDaftarPesananHide.visibility = if (adapterKeranjangHide.itemCount == 0) View.GONE else View.VISIBLE
        }
    }

    private fun updateTotalHarga() {
        var sumPrice: Long = 0
        var totalItem = 0

        for (keranjang in dataKeranjang) {
            if (keranjang.keranjangModel!!.diPilih) {
                val hargaInt = keranjang.produkModel?.hargaProduk!!
                val jumlahHarga = hargaInt * keranjang.keranjangModel!!.jumlahProduk!!
                sumPrice += jumlahHarga
                totalItem++
            }
        }

//        val selectedSize = dataKeranjang.filter { it.keranjangModel!!.diPilih }.size
        val selectedKeranjang = dataKeranjang.any { it.keranjangModel!!.diPilih }
        val txtDelDiPilih = "$totalItem produk terpilih"
        val btnTxt = "${getString(R.string.pesan_sekarang)} ($totalItem)"
        val ttlHrgBlnj = "Rp${addcoma3digit(sumPrice)}"

        with(binding) {
            totalHarga.text = if (selectedKeranjang) ttlHrgBlnj else getString(R.string.strip)
            llProdukSelected.visibility = if (!selectedKeranjang) View.GONE else View.VISIBLE
            btnDeleteDiCeklis.visibility = if (!selectedKeranjang) View.GONE else View.VISIBLE
            btnPesanSekarang.isEnabled = selectedKeranjang
            xProdukTerpilih.text = txtDelDiPilih
            btnPesanSekarang.text = if (selectedKeranjang) btnTxt else getString(R.string.pesan_sekarang)

            btnDeleteDiCeklis.setOnClickListener { showAlertDialog(totalItem, false) }
            btnDelallProdukDihide.setOnClickListener { showAlertDialog(dataKeranjangHide.size, true) }
        }
    }

    private fun showAlertDialog(totalItem: Int, deleteHide: Boolean) {
        val title = if (!deleteHide) "Hapus $totalItem produk?" else "Hapus $totalItem produk tidak dapat diproses?"
        val msg = if (!deleteHide) getString(R.string.hapus_produk_dipilih) else "Semua produk ini akan dihapus dari keranjangmu"
        val positifBtn = if (!deleteHide) getString(R.string.hapus) else getString(R.string.hapus_semua)

        val alertdialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setNegativeButton(getString(R.string.batalkan)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(positifBtn) { _, _ ->
                deleteSelected(deleteHide)
            }.show()

        val negativeBtn = alertdialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val positiveBtn = alertdialog.getButton(DialogInterface.BUTTON_POSITIVE)

        negativeBtn.apply {
            textSize = 16f
            setTextColor(context.getColor(R.color.grey_500))
        }

        positiveBtn.apply {
            textSize = 16f
            setTextColor(context.getColor(R.color.error))
        }
    }

    private fun deleteSelected(deleteHide: Boolean) {
        val selectedKeranjang = dataKeranjang.filter { it.keranjangModel?.diPilih == true }

        if (HelperConnection.isConnected(this))
            if (!deleteHide)
                selectedKeranjang.forEach {
                    keranjangRef.child("${it.produkModel?.idProduk}").removeValue()
                        .addOnSuccessListener { getDataKeranjang() }
                }
            else
                dataKeranjangHide.forEach {
                    keranjangRef.child("${it.produkModel?.idProduk}").removeValue()
                        .addOnSuccessListener { getDataKeranjang() }
                }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) startActivity(Intent(this, LoginActivity::class.java))
    }
}