package com.bekado.bekadoonline.ui.transaksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterCheckout
import com.bekado.bekadoonline.databinding.ActivityDetailTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bekado.bekadoonline.model.ProdukModel
import com.bekado.bekadoonline.model.TransaksiModel
import com.bekado.bekadoonline.ui.PembayaranActivity
import com.example.testnew.model.KeranjangModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DetailTransaksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransaksiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private var dataCombin: ArrayList<CombinedKeranjangModel> = ArrayList()

    private lateinit var trxRef: DatabaseReference

    private lateinit var transaksi: TransaksiModel
    private var isAdmin: Boolean = false
    private var showAllItem = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        trxRef = db.getReference("transaksi")

        transaksi = intent.getParcelableExtra("trx") ?: TransaksiModel()
        isAdmin = intent.getBooleanExtra("isAdmin", false)

        val uidNow = auth.currentUser?.uid
        val timeBuy = "${convertTstmp(transaksi.timestamp!!.toLong())} WIB"

        getDataProduk(uidNow, transaksi.idTransaksi)
        dataTransaksi(uidNow, transaksi.idTransaksi)

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }

            status.text = transaksi.statusPesanan
            noPesanan.text = transaksi.noPesanan
            waktuPembelian.text = timeBuy

            lihatPembayaran.setOnClickListener { startActivity(Intent(this@DetailTransaksiActivity, PembayaranActivity::class.java)) }
        }
    }

    private fun dataTransaksi(uidNow: String?, idTransaksi: String?) {
        if (!isAdmin) {
            trxRef.child("$uidNow/$idTransaksi").get().addOnSuccessListener { snapshot ->
                setDataView(snapshot)
//                alamatPenerima()
            }
        } else {
            trxRef.get().addOnSuccessListener { data ->
                for (item in data.children) {
                    val snapshot = item.child("$idTransaksi")
                    if (snapshot.exists()) {
                        setDataView(snapshot)
//                        alamatPenerima()
                        getProfilData("${item.key}")
                    }
                }
            }
        }
    }

//    private fun alamatPenerima() {
//        trxRef.child("alamat").get().addOnSuccessListener { snapshot ->
//            val nama = snapshot.child("nama").value.toString()
//            val noHp = snapshot.child("noHp").value.toString()
//            val alamat = snapshot.child("alamatLengkap").value.toString()
//            val kodePos = snapshot.child("kodePos").value.toString()
//
//            val address = "$alamat, $kodePos"
//            binding.alamatPenerima1.text = nama
//            binding.alamatPenerima2.text = noHp
//            binding.alamatPenerima3.text = address
//        }
//    }

    private fun setDataView(snapshot: DataSnapshot) {
        val rp = snapshot.child("currency").value as String
        val totalItem = snapshot.child("totalItem").value as Long
        val ttlHarga = snapshot.child("totalHarga").value as Long
        val ongkir = snapshot.child("ongkir").value as Long
        val metodePembayaran = snapshot.child("metodePembayaran").value as String
        val totalBelanja = snapshot.child("totalBelanja").value as Long

        val ttlPrdk = "${getString(R.string.total_harga)} ($totalItem produk)"
        val ttlHrg = rp + Helper.addcoma3digit(ttlHarga)
        val ongkr = if (ongkir > 0) rp + Helper.addcoma3digit(ongkir) else "Gratis"
        val ttlBlnj = if (totalBelanja >= 1) rp + Helper.addcoma3digit(totalBelanja) else "Gratis"

        with(binding) {
            xProduk.text = ttlPrdk
            totalHarga.text = ttlHrg
            pembayaranMetodeTxt.text = metodePembayaran
            ongkirHarga.text = ongkr
            totalBelanjaHarga.text = ttlBlnj
        }
    }

    private fun getProfilData(uid: String) {

    }

    private fun getDataProduk(uidNow: String?, idTransaksi: String?) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.layoutManager = layoutManager

        val reference = if (!isAdmin) "transaksi/$uidNow" else "transaksi"
        val ref = db.getReference(reference)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdmin) {
                    for (produk in snapshot.child("$idTransaksi/produkList").children) {
                        val dataProduk = produk.getValue(ProdukModel::class.java)
                        val dataKeranjang = produk.getValue(KeranjangModel::class.java)
                        dataCombin.add(CombinedKeranjangModel(dataProduk, dataKeranjang))
                    }
                } else {
                    for (data in snapshot.children) {
                        for (produk in data.child("$idTransaksi/produkList").children) {
                            val dataProduk = produk.getValue(ProdukModel::class.java)
                            val dataKeranjang = produk.getValue(KeranjangModel::class.java)
                            dataCombin.add(CombinedKeranjangModel(dataProduk, dataKeranjang))
                        }
                    }
                }

                val adapterDaftarProduk = AdapterCheckout(ArrayList<CombinedKeranjangModel>().apply { add(dataCombin[0]) })
                binding.rvDaftarProduk.adapter = adapterDaftarProduk

                val txt = "+${dataCombin.size - 1} produk lainnya"
                val showD = R.drawable.icon_round_expand_more_24
                val hideD = R.drawable.icon_round_expand_less_24

                binding.buttonShowAll.apply {
                    visibility = if (dataCombin.size <= 1) View.GONE else View.VISIBLE
                    text = txt
                    setIconResource(showD)
                    setOnClickListener {
                        showAllItem = !showAllItem
                        if (showAllItem) {
                            adapterDaftarProduk.checkoutModelList = dataCombin
                            binding.buttonShowAll.text = getString(R.string.tampilkan_lebih_sedikit)
                            binding.buttonShowAll.setIconResource(hideD)
                        } else {
                            adapterDaftarProduk.checkoutModelList = ArrayList<CombinedKeranjangModel>().apply {
                                add(dataCombin[0])
                            }
                            binding.buttonShowAll.text = txt
                            binding.buttonShowAll.setIconResource(showD)
                        }

                        adapterDaftarProduk.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun convertTstmp(trxTimestamp: Long): String {
        val sdfTanggal = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
        Calendar.getInstance().timeInMillis = trxTimestamp

        return sdfTanggal.format(Date(trxTimestamp))
    }
}