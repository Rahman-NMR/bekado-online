package com.bekado.bekadoonline.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.repositories.TrxDetailRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class TrxDetailRepositoryImpl(db: FirebaseDatabase, private val storage: FirebaseStorage) : TrxDetailRepository {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _detailTransaksi = MutableLiveData<TrxDetailModel?>()
    private val detailTransaksi: LiveData<TrxDetailModel?> get() = _detailTransaksi

    private val _alamatModel = MutableLiveData<AlamatModel?>()
    private val alamatModel: LiveData<AlamatModel?> get() = _alamatModel

    private val _paymentModel = MutableLiveData<BuktiPembayaranModel?>()
    private val paymentModel: LiveData<BuktiPembayaranModel?> get() = _paymentModel

    private val _produkModelList = MutableLiveData<ArrayList<CombinedKeranjangModel>?>()
    private val produkModelList: LiveData<ArrayList<CombinedKeranjangModel>?> get() = _produkModelList

    private val _akunOwnerTrx = MutableLiveData<AkunModel?>()
    private val akunOwnerTrx: LiveData<AkunModel?> get() = _akunOwnerTrx

    private var akunListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val akun = snapshot.getValue(AkunModel::class.java)
            _akunOwnerTrx.value = akun
        }

        override fun onCancelled(error: DatabaseError) {
            _akunOwnerTrx.value = null
        }
    }

    private var trxDetailLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _isLoading.value = true

            try {
                insertDetailTransaksi(snapshot)
                insertAlamat(snapshot)
                insertPayment(snapshot)
                insertProdukList(snapshot)
                _isLoading.value = false
            } catch (e: Exception) {
                _detailTransaksi.value = null
                _alamatModel.value = null
                _paymentModel.value = null
                _produkModelList.value = null
                _isLoading.value = true
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _detailTransaksi.value = null
            _alamatModel.value = null
            _paymentModel.value = null
            _produkModelList.value = null
            _isLoading.value = true
        }
    }

    private fun insertDetailTransaksi(snapshot: DataSnapshot) {
        val transaksi = snapshot.getValue(TrxDetailModel::class.java)
        _detailTransaksi.value = transaksi
    }

    private fun insertAlamat(snapshot: DataSnapshot) {
        val alamat = snapshot.child("alamatPenerima").getValue(AlamatModel::class.java)
        _alamatModel.value = alamat
    }

    private fun insertPayment(snapshot: DataSnapshot) {
        val payment = snapshot.child("buktiTransaksi").getValue(BuktiPembayaranModel::class.java)
        _paymentModel.value = payment
    }

    private fun insertProdukList(snapshot: DataSnapshot) {
        val produkData = ArrayList<CombinedKeranjangModel>()
        for (item in snapshot.child("produkList").children) {
            val produk = item.getValue(ProdukModel::class.java)
            val keranjang = item.getValue(KeranjangModel::class.java)
            produkData.add(CombinedKeranjangModel(produk, keranjang))
        }
        _produkModelList.value = produkData
    }

    private val transaksiRef = db.getReference("transaksi")
    private val akunRef = db.getReference("akun")

    private fun startListener(pathDetailTrx: String?) {
        val detailTrxPath = pathDetailTrx ?: ""
        val uidOwnerTrx = pathDetailTrx?.split("/")?.get(0) ?: ""

        transaksiRef.child(detailTrxPath).removeEventListener(trxDetailLstnrUsr)
        transaksiRef.child(detailTrxPath).addValueEventListener(trxDetailLstnrUsr)

        akunRef.child(uidOwnerTrx).removeEventListener(akunListener)
        akunRef.child(uidOwnerTrx).addValueEventListener(akunListener)
    }

    override fun getDetailTransaksi(pathDetailTrx: String?): LiveData<TrxDetailModel?> {
        startListener(pathDetailTrx)
        return detailTransaksi
    }

    override fun getLoading(): LiveData<Boolean> = isLoading
    override fun getAlamat(): LiveData<AlamatModel?> = alamatModel
    override fun getPayment(): LiveData<BuktiPembayaranModel?> = paymentModel
    override fun getProdukList(): LiveData<ArrayList<CombinedKeranjangModel>?> = produkModelList
    override fun getDataAkunOwner(): LiveData<AkunModel?> = akunOwnerTrx

    override fun uploadBuktiPembayaran(imageUri: Uri, statusPesanan: String, pathDetailTrx: String?, response: (Boolean) -> Unit) {
        val storageRef = storage.getReference("transaksi/$pathDetailTrx.jpg")

        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnCompleteListener { task ->
                val imgLink = task.result.toString()
                transaksiRef.child("$pathDetailTrx/buktiTransaksi/buktiTransaksi").setValue(imgLink).addOnCompleteListener {
                    transaksiRef.child("$pathDetailTrx/statusPesanan").setValue(statusPesanan)
                    response.invoke(it.isSuccessful)
                }.addOnFailureListener { response.invoke(false) }
            }
        }.addOnFailureListener { response.invoke(false) }
    }

    override fun removeListener(pathDetailTrx: String?) {
        val detailTrxPath = pathDetailTrx ?: ""
        val uidOwnerTrx = pathDetailTrx?.split("/")?.get(0) ?: ""

        transaksiRef.child(detailTrxPath).removeEventListener(trxDetailLstnrUsr)
        akunRef.child(uidOwnerTrx).removeEventListener(akunListener)
    }
}