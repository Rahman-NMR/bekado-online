package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrxRepositoryImpl(private val auth: FirebaseAuth, private val db: FirebaseDatabase) : TrxRepository {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _trxListModel = MutableLiveData<ArrayList<TrxListModel>?>()
    private val trxListModel: LiveData<ArrayList<TrxListModel>?> get() = _trxListModel

    private val _totalTransaksi = MutableLiveData<TrxCountModel?>()
    private val totalTransaksiData: LiveData<TrxCountModel?> get() = _totalTransaksi

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TrxListModel>()
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    getDataTransaksi(userTransaksi, item, dataTrx)
                    when (item.child("parentStatus").value.toString()) {
                        "antrian" -> totalAntrian += 1
                        "proses" -> totalProses += 1
                        "selesai" -> totalSelesai += 1
                    }
                }
            }

            dataTrx.sortByDescending { it.timestamp }
            _trxListModel.value = dataTrx
            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private var trxLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TrxListModel>()
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (item in dataSnapshot.children) {
                getDataTransaksi(dataSnapshot, item, dataTrx)
                when (item.child("parentStatus").value.toString()) {
                    "antrian" -> totalAntrian += 1
                    "proses" -> totalProses += 1
                    "selesai" -> totalSelesai += 1
                }
            }

            dataTrx.sortByDescending { it.timestamp }
            _trxListModel.value = dataTrx
            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private fun returnValue(totalAntrian: Int, totalProses: Int, totalSelesai: Int) {
        _totalTransaksi.value = TrxCountModel(totalAntrian, totalProses, totalSelesai)
        _isLoading.value = false
    }

    private fun errorValue() {
        _trxListModel.value = null
        _totalTransaksi.value = TrxCountModel(0, 0, 0)
        _isLoading.value = true
    }

    private fun getDataTransaksi(userKey: DataSnapshot, item: DataSnapshot, dataTransaksi: ArrayList<TrxListModel>) {
        val uidOwnerTrx = userKey.key
        val idTrx = item.child("idTransaksi").value.toString()
        val noPsnn = item.child("noPesanan").value.toString()
        val timestp = item.child("timestamp").value.toString()
        val sttsPsnn = item.child("statusPesanan").value.toString()
        val currency = item.child("currency").value.toString()
        val totalBlnj = item.child("totalBelanja").value as? Long ?: 0L
        val produkList = item.child("produkList").children.sortedBy { it.child("timestamp").value.toString() }.toList()
        val lastProduk = produkList.firstOrNull()
        val fotoPrdk = lastProduk?.child("fotoProduk")?.value?.toString() ?: ""
        val namaPrdk = lastProduk?.child("namaProduk")?.value?.toString() ?: ""
        val jmlhPrdk = lastProduk?.child("jumlahProduk")?.value as? Long ?: 0L
        val lainnya = item.child("totalItem").value as? Long ?: 0L
        val transaksi = TrxListModel(uidOwnerTrx, idTrx, noPsnn, timestp, sttsPsnn, fotoPrdk, namaPrdk, jmlhPrdk, currency, totalBlnj, lainnya)

        dataTransaksi.add(transaksi)
    }

    private fun trxRef(akunModel: AkunModel?): DatabaseReference {
        return if (akunModel?.statusAdmin == true) db.getReference("transaksi")
        else db.getReference("transaksi/${akunModel?.uid}")
    }

    private fun startTransaksiListener(akunModel: AkunModel?) {
        if (akunModel?.statusAdmin == true) {
            trxRef(akunModel).removeEventListener(trxLstnrAdm)
            trxRef(akunModel).addValueEventListener(trxLstnrAdm)
        } else {
            trxRef(akunModel).removeEventListener(trxLstnrUsr)
            trxRef(akunModel).addValueEventListener(trxLstnrUsr)
        }
    }

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TrxListModel>?> {
        startTransaksiListener(akunModel)
        return trxListModel
    }

    override fun getTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        startTransaksiListener(akunModel)
        return totalTransaksiData
    }

    override fun removeListener(akunModel: AkunModel?) {
        if (akunModel?.statusAdmin == true) trxRef(akunModel).removeEventListener(trxLstnrAdm)
        else trxRef(akunModel).removeEventListener(trxLstnrUsr)
    }

    override fun createTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val dataBuktiTrx = hashMapOf<String, Any>()
            mapOfBuktiTransaksi(dataBuktiTrx, totalBelanja) { isSuccessful ->
                if (isSuccessful) {
                    val transaksiRef = db.getReference("transaksi/${currentUser.uid}")
                    val idTransaksi = transaksiRef.push().key.toString()
                    val mapOfDataTransaksi = dataTransaksi.toMap(idTransaksi)

                    val fullDataTransaksi = hashMapOf<String, Any>()
                    fullDataTransaksi.putAll(mapOfDataTransaksi)
                    fullDataTransaksi["alamatPenerima"] = dataAlamat
                    fullDataTransaksi["buktiTransaksi"] = dataBuktiTrx
                    fullDataTransaksi["produkList"] = produkMap
                    transaksiRef.child(idTransaksi).setValue(fullDataTransaksi)
                        .addOnCompleteListener { response(it.isSuccessful) }
                        .addOnFailureListener { response(false) }
                } else response(false)
            }
        } else response(false)
    }

    private fun TrxDetailModel.toMap(idTransaksi: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["currency"] = this.currency ?: ""
        map["idTransaksi"] = idTransaksi
        map["metodePembayaran"] = this.metodePembayaran ?: ""
        map["noPesanan"] = this.noPesanan ?: ""
        map["ongkir"] = this.ongkir ?: 0L
        map["parentStatus"] = this.parentStatus ?: ""
        map["statusPesanan"] = this.statusPesanan ?: ""
        map["timestamp"] = this.timestamp ?: ""
        map["totalBelanja"] = this.totalBelanja ?: 0L
        map["totalHarga"] = this.totalHarga ?: 0L
        map["totalItem"] = this.totalItem ?: 0L
        return map
    }

    private fun mapOfBuktiTransaksi(dataBuktiTrx: HashMap<String, Any>, totalBelanja: Long, response: (Boolean) -> Unit) {
        db.getReference("aaabdfiklnrstt").get().addOnCompleteListener { data ->
            if (data.result.exists()) {
                dataBuktiTrx["pemilikBank"] = data.result.child("pemilik").value as String
                dataBuktiTrx["biayaTransfer"] = totalBelanja
                dataBuktiTrx["fotoBank"] = data.result.child("logoWiki").value as String
                dataBuktiTrx["namaBank"] = data.result.child("name").value as String
                dataBuktiTrx["noRek"] = data.result.child("noRek").value as String
                response.invoke(data.isSuccessful)
            } else response.invoke(false)
        }.addOnFailureListener { response.invoke(false) }
    }
}