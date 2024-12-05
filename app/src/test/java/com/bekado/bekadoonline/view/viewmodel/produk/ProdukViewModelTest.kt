package com.bekado.bekadoonline.view.viewmodel.produk

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.ProductUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProdukViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var productUseCase: ProductUseCase

    private lateinit var produkViewModel: ProdukViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        produkViewModel = ProdukViewModel(productUseCase)
    }

    @Test
    fun `test getDataProduk success`() {
        val livedata = MutableLiveData<ArrayList<ProdukModel>?>()
        livedata.value = arrayListOf(
            ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
            ProdukModel(namaProduk = "Produk B", hargaProduk = 20000)
        )

        `when`(productUseCase.executeGetAllDataProduk()).thenReturn(livedata)

        val actualProduk = produkViewModel.getAllProduk().value

        assertNotNull(actualProduk)
        assertEquals(2, actualProduk?.size)
    }

    @Test
    fun `test getFilterByKategori success`() {
        val livedata = MutableLiveData<ArrayList<ButtonModel>?>()
        livedata.value = arrayListOf(
            ButtonModel(idKategori = "0", namaKategori = "Semua", isActive = true),
            ButtonModel(idKategori = "1", namaKategori = "Makanan", isActive = false),
            ButtonModel(idKategori = "2", namaKategori = "Minuman", isActive = false)
        )

        `when`(productUseCase.executeFilterByKategori()).thenReturn(livedata)

        val actualKategori = produkViewModel.filterByKategori().value
        val activeCount = actualKategori?.count { it.isActive }

        assertNotNull(actualKategori)
        assertTrue(activeCount == 1)
        assertNotNull(actualKategori)
        assertEquals(3, actualKategori?.size)
    }

    @Test
    fun `test searchProduk with matching query`() {
        val dataProduk = arrayListOf(
            ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
            ProdukModel(namaProduk = "Produk B", hargaProduk = 20000)
        )

        val searchText = "Produk A"
        val searchResult = produkViewModel.searchProduk(dataProduk, searchText)

        assertEquals(1, searchResult.size)
        assertEquals(searchText, searchResult[0].namaProduk)
    }
}