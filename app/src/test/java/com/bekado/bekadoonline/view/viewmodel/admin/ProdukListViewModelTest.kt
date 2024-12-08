package com.bekado.bekadoonline.view.viewmodel.admin

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
class ProdukListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var adminUseCase: AdminUseCase

    private lateinit var produkListViewModel: ProdukListViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        produkListViewModel = ProdukListViewModel(adminUseCase)
    }

    @Test
    fun `when getProdukList is called, should not null and return data`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val dummyProdukList = arrayListOf(
            ProdukModel(idProduk = "1", namaProduk = "Nasi Goreng", hargaProduk = 10000, idKategori = dummyKategori.idKategori),
            ProdukModel(idProduk = "2", namaProduk = "Mie Goreng", hargaProduk = 12000, idKategori = dummyKategori.idKategori),
            ProdukModel(idProduk = "3", namaProduk = "Sate Ayam", hargaProduk = 15000, idKategori = dummyKategori.idKategori)
        )
        val livedata = MutableLiveData<ArrayList<ProdukModel>>()
        livedata.value = dummyProdukList

        `when`(adminUseCase.executeGetDataProdukList(dummyKategori.idKategori)).thenReturn(livedata)

        val actualProdukList = produkListViewModel.getProdukList(dummyKategori.idKategori).value
        val idKategoriCheck = actualProdukList?.all { it.idKategori == dummyKategori.idKategori }

        verify(adminUseCase).executeGetDataProdukList(dummyKategori.idKategori)
        assertNotNull(actualProdukList)
        assertEquals(dummyProdukList, actualProdukList)
        assertEquals(3, actualProdukList?.size)
        assertTrue(idKategoriCheck ?: false)
    }

    @Test
    fun `when getProdukList is called, should null and return null`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val livedata = MutableLiveData<ArrayList<ProdukModel>>()
        livedata.value = null

        `when`(adminUseCase.executeGetDataProdukList(dummyKategori.idKategori)).thenReturn(livedata)

        val actualProdukList = produkListViewModel.getProdukList(dummyKategori.idKategori).value

        verify(adminUseCase).executeGetDataProdukList(dummyKategori.idKategori)
        assertNull(actualProdukList)
        assertNull(actualProdukList?.get(0)?.idProduk)
    }

    @Test
    fun `when updateVisibilitasProduk is called, response should be invoked`() {
        val idProduk = "1"
        val visibility = true
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeUpdateVisibilityProduk(idProduk, visibility, response)

        produkListViewModel.updateVisibilitasProduk(idProduk, visibility, response)

        verify(adminUseCase).executeUpdateVisibilityProduk(idProduk, visibility, response)
        verify(response).invoke(true)
    }
}