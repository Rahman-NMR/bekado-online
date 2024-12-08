package com.bekado.bekadoonline.view.viewmodel.admin

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.AdminUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProdukViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var adminUseCase: AdminUseCase

    private lateinit var produkViewModel: ProdukViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        produkViewModel = ProdukViewModel(adminUseCase)
    }

    @Test
    fun `when getDataProduk is called, should not null and return data`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val dummyProduk = ProdukModel(idProduk = "1", namaProduk = "Nasi Goreng", hargaProduk = 10000, idKategori = dummyKategori.idKategori)
        val livedata = MutableLiveData<ProdukModel>()
        livedata.value = dummyProduk

        `when`(adminUseCase.executeGetDataProduk(dummyProduk.idProduk)).thenReturn(livedata)

        val actualProduk = produkViewModel.getDataProduk(dummyProduk.idProduk).value

        verify(adminUseCase).executeGetDataProduk(dummyProduk.idProduk)
        assertNotNull(actualProduk)
        assertEquals(dummyProduk, actualProduk)
        assertEquals(dummyKategori.idKategori, actualProduk?.idKategori)
    }

    @Test
    fun `when getDataProduk is called, should null and return null`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val livedata = MutableLiveData<ProdukModel>()
        livedata.value = null

        `when`(adminUseCase.executeGetDataProduk(dummyKategori.idKategori)).thenReturn(livedata)

        val actualProduk = produkViewModel.getDataProduk(dummyKategori.idKategori).value

        verify(adminUseCase).executeGetDataProduk(dummyKategori.idKategori)
        assertNull(actualProduk)
    }

    @Test
    fun `when updateDataProduk is called, response should be invoked`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val isEdit = true
        val idProduk = "1"
        val imageUri = Mockito.mock(Uri::class.java)
        val idKategori = dummyKategori.idKategori
        val namaProduk = "Nasi Goreng"
        val hargaProduk = 10000L
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(6)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeUpdateDetailProduk(isEdit, imageUri, idProduk, idKategori, namaProduk, hargaProduk, response)

        produkViewModel.updateDataProduk(isEdit, imageUri, idProduk, idKategori, namaProduk, hargaProduk, response)

        verify(adminUseCase).executeUpdateDetailProduk(isEdit, imageUri, idProduk, idKategori, namaProduk, hargaProduk, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when deleteProduk is called, response should be invoked`() {
        val idProduk = "1"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeDeleteProduk(idProduk, response)

        produkViewModel.deleteProduk(idProduk, response)

        verify(adminUseCase).executeDeleteProduk(idProduk, response)
        verify(response).invoke(true)
    }
}