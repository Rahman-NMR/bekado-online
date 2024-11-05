package com.bekado.bekadoonline.domain.interactor

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.KategoriListRepository
import com.bekado.bekadoonline.domain.repositories.KategoriRepository
import com.bekado.bekadoonline.domain.repositories.ProdukListRepository
import com.bekado.bekadoonline.domain.repositories.ProdukRepository
import com.bekado.bekadoonline.domain.usecase.AdminUseCase
import java.util.ArrayList

class AdminUseCaseInteractor(
    private val kategoriRepository: KategoriRepository,
    private val kategoriListRepository: KategoriListRepository,
    private val produkRepository: ProdukRepository,
    private val produkListRepository: ProdukListRepository
) : AdminUseCase {
    override fun executeLoadingKategoriList(): LiveData<Boolean> {
        return kategoriListRepository.getLoading()
    }

    override fun executeGetDataKategoriList(): LiveData<ArrayList<KategoriModel>?> {
        return kategoriListRepository.getDataKategoriList()
    }

    override fun executeAddNewKategori(namaKategori: String, posisi: Long, response: (Boolean) -> Unit) {
        kategoriListRepository.addNewKategori(namaKategori, posisi, response)
    }

    override fun executeUpdateNamaKategori(idKategori: String?, namaKategori: String, response: (Boolean) -> Unit) {
        kategoriListRepository.updateNamaKategori(idKategori, namaKategori, response)
    }

    override fun executeUpdateVisibilitasKategori(idKategori: String?, visibilitas: Boolean, response: (Boolean) -> Unit) {
        kategoriListRepository.updateVisibilitasKategori(idKategori, visibilitas, response)
    }

    override fun executeRemoveListenerKategoriList() {
        kategoriListRepository.removeListenerKategoriList()
    }

    override fun executeLoadingProdukList(): LiveData<Boolean> {
        return produkListRepository.getLoading()
    }

    override fun executeGetDataProdukList(idKategori: String?): LiveData<ArrayList<ProdukModel>?> {
        return produkListRepository.getDataProdukList(idKategori)
    }

    override fun executeUpdateVisibilityProduk(idProduk: String?, visibility: Boolean, response: (Boolean) -> Unit) {
        produkListRepository.updateVisibilityProduk(idProduk, visibility, response)
    }

    override fun executeRemoveListenerProdukList(idKategori: String?) {
        produkListRepository.removeListenerProdukList(idKategori)
    }

    override fun executeLoadingKategori(): LiveData<Boolean> {
        return kategoriRepository.getLoading()
    }

    override fun executeGetDataKategori(extraIdKategori: String?): LiveData<KategoriModel?> {
        return kategoriRepository.getDataKategori(extraIdKategori)
    }

    override fun executeDeleteKategori(idKategori: String?, response: (Boolean) -> Unit) {
        kategoriRepository.deleteKategori(idKategori, response)
    }

    override fun executeRemoveListenerKategori(idKategori: String?) {
        kategoriRepository.removeListenerKategori(idKategori)
    }

    override fun executeLoadingProduk(): LiveData<Boolean> {
        return produkRepository.getLoading()
    }

    override fun executeGetDataProduk(idProduk: String?): LiveData<ProdukModel?> {
        return produkRepository.getDataProduk(idProduk)
    }

    override fun executeUpdateDetailProduk(
        isEdit: Boolean,
        imageUri: Uri?,
        idProduk: String?,
        idKategori: String?,
        namaProduk: String,
        hargaProduk: Long,
        response: (Boolean) -> Unit
    ) {
        produkRepository.updateDetailProduk(
            isEdit = isEdit,
            idProduk = idProduk,
            imageUri = imageUri,
            idKategori = idKategori,
            namaProduk = namaProduk,
            hargaProduk = hargaProduk,
            response = response)
    }

    override fun executeDeleteProduk(idProduk: String?, response: (Boolean) -> Unit) {
        produkRepository.deleteProduk(idProduk, response)
    }

    override fun executeRemoveListenerProduk(idProduk: String?) {
        produkRepository.removeListenerProduk(idProduk)
    }
}