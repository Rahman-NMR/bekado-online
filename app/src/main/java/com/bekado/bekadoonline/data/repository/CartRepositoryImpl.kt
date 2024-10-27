package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class CartRepositoryImpl(auth: FirebaseAuth, db: FirebaseDatabase) : CartRepository {
    private val keranjangRef = db.getReference("keranjang/${auth.currentUser?.uid}")
    private val produkRef = db.getReference("produk/produk")

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _keranjangModel = MutableLiveData<ArrayList<CombinedKeranjangModel>?>()
    private val keranjangModel: LiveData<ArrayList<CombinedKeranjangModel>?> get() = _keranjangModel

//    private val produkListeners = mutableMapOf<String, ValueEventListener>()

    private var keranjangListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(keranjangSnapshot: DataSnapshot) {
//            val keranjangItem = arrayListOf<CombinedKeranjangModel>()

            _isLoading.value = true
            if (keranjangSnapshot.exists()) {
//                for (keranjangList in keranjangSnapshot.children) {
                val idProduk = keranjangSnapshot.children.mapNotNull { it.key }.toList()//keranjangList.child("idProduk").getValue(String::class.java)

                dataProduk(idProduk, keranjangSnapshot/*keranjangList*/)
//                }
            } else {
                _keranjangModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _keranjangModel.value = null
            _isLoading.value = true
        }
    }

    private fun dataProduk(keranjangKey: List<String>, keranjangList: DataSnapshot) {
        produkRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(produkSnapshot: DataSnapshot) {
                val keranjangItem = mutableMapOf<String, CombinedKeranjangModel>()

                for (idProduk in keranjangKey) {
                    val dataKeranjang = keranjangList.child(idProduk).getValue(KeranjangModel::class.java)
                    val dataProduk = produkSnapshot.child(idProduk).getValue(ProdukModel::class.java)

                    if (dataKeranjang != null && dataProduk != null) {
                        keranjangItem[idProduk] = CombinedKeranjangModel(dataProduk, dataKeranjang)
                    }
                }

                keranjangItem.values.toList().let { _keranjangModel.value = ArrayList(it) }
//                val keranjangItems = keranjangItem.values.toList() as ArrayList<CombinedKeranjangModel>
//                _keranjangModel.value = keranjangItems
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _keranjangModel.value = null
                _isLoading.value = true
            }

        })
        /*if (idProduk != null) {
            val dataKeranjang = keranjangList.getValue(KeranjangModel::class.java)
            val produkListener = object : ValueEventListener {
                override fun onDataChange(produkSnapshot: DataSnapshot) {
                    val dataProduk = produkSnapshot.getValue(ProdukModel::class.java)
                    keranjangItem.removeAll { it.produkModel?.idProduk == dataProduk?.idProduk }
                    keranjangItem.add(CombinedKeranjangModel(dataProduk, dataKeranjang))

                    _keranjangModel.value = keranjangItem
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            produkRef.child(idProduk).addValueEventListener(produkListener)
            produkListeners[idProduk] = produkListener
        }*/
    }

    private fun clearKeranjangListeners() {
        keranjangRef.orderByChild("timestamp").removeEventListener(keranjangListener)

//        produkListeners.forEach { (idProduk, listener) -> produkRef.child(idProduk).removeEventListener(listener) }
//        produkListeners.clear()
    }

    init {
        startListener()
    }

    override fun getDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?> = keranjangModel
    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun updateJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit) {
        keranjangRef.child("$path/jumlahProduk").get()
            .addOnSuccessListener { dataSnapshot ->
                val jumlahSekarang = dataSnapshot.getValue(Long::class.java) ?: 0
                val jumlahProduk = if (isPlus) jumlahSekarang + 1 else jumlahSekarang - 1

                keranjangRef.child("$path/jumlahProduk").setValue(jumlahProduk)
            }.addOnFailureListener { response.invoke(false) }
    }

    override fun updateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit) {
        if (!idProduk.isNullOrEmpty()) {
            keranjangRef.child("$idProduk/diPilih").setValue(isChecked)
                .addOnCompleteListener { response.invoke(it.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun deleteThisProduk(idProduk: String?, response: (Boolean) -> Unit) {
        if (!idProduk.isNullOrEmpty()) {
            keranjangRef.child(idProduk).removeValue()
                .addOnCompleteListener { response.invoke(it.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun deleteSelectedProduk(selectedKeranjang: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit) {
        clearKeranjangListeners()
        selectedKeranjang?.forEach { keranjang ->
            keranjangRef.child("${keranjang.produkModel?.idProduk}").removeValue()
                .addOnCompleteListener {
                    response.invoke(it.isSuccessful)
                    startListener()
                }.addOnFailureListener {
                    response.invoke(false)
                    startListener()
                }
        }
    }

    override fun cancelAction(itemKeranjang: CombinedKeranjangModel) = run {
        val path = "${itemKeranjang.produkModel?.idProduk}"

        itemKeranjang.keranjangModel?.let { keranjangModel ->
            val restoreData = mapOf(
                "idProduk" to itemKeranjang.produkModel?.idProduk,
                "jumlahProduk" to keranjangModel.jumlahProduk,
                "timestamp" to keranjangModel.timestamp,
                "diPilih" to keranjangModel.diPilih
            )

            keranjangRef.child(path).setValue(restoreData)
        }
    }

    override fun startListener() {
        keranjangRef.orderByChild("timestamp").addValueEventListener(keranjangListener)
    }

    override fun removeListener() {
        clearKeranjangListeners()
    }
}