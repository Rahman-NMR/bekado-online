package com.bekado.bekadoonline.view.viewmodel.produk

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.ProductUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

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
    fun `when getDataProduk is called, should not null and return data`() {
        val dummyProduk = arrayListOf(
            ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
            ProdukModel(namaProduk = "Produk B", hargaProduk = 20000)
        )
        val livedata = MutableLiveData<ArrayList<ProdukModel>?>()
        livedata.value = dummyProduk

        `when`(productUseCase.executeGetAllDataProduk()).thenReturn(livedata)

        val actualProduk = produkViewModel.getAllProduk().value

        verify(productUseCase).executeGetAllDataProduk()
        assertNotNull(actualProduk)
        assertEquals(2, actualProduk?.size)
        assertEquals(dummyProduk, actualProduk)
    }

    @Test
    fun `when getDataProduk is called, should null and return null`() {
        val livedata = MutableLiveData<ArrayList<ProdukModel>?>()
        livedata.value = null

        `when`(productUseCase.executeGetAllDataProduk()).thenReturn(livedata)

        val actualProduk = produkViewModel.getAllProduk().value

        verify(productUseCase).executeGetAllDataProduk()
        assertNull(actualProduk)
        assertNull(actualProduk?.get(1)?.namaProduk)
    }

    @Test
    fun `when getFilterByKategori is called, should not null and return data is active 1`() {
        val dummyKategori = arrayListOf(
            ButtonModel(idKategori = "0", namaKategori = "Semua", isActive = false),
            ButtonModel(idKategori = "1", namaKategori = "Makanan", isActive = true),
            ButtonModel(idKategori = "2", namaKategori = "Minuman", isActive = false)
        )
        val livedata = MutableLiveData<ArrayList<ButtonModel>?>()
        livedata.value = dummyKategori

        `when`(productUseCase.executeFilterByKategori()).thenReturn(livedata)

        val actualKategori = produkViewModel.filterByKategori().value
        val activeCount = actualKategori?.count { it.isActive }
        val activeKategori = actualKategori?.find { it.isActive }

        verify(productUseCase).executeFilterByKategori()
        assertNotNull(actualKategori)
        assertTrue(activeCount == 1)
        assertEquals(3, actualKategori?.size)
        assertEquals(dummyKategori, actualKategori)
        assertEquals("Makanan", activeKategori?.namaKategori)
    }

    @Test
    fun `when getFilterByKategori is called, should null and return null`() {
        val livedata = MutableLiveData<ArrayList<ButtonModel>?>()
        livedata.value = null

        `when`(productUseCase.executeFilterByKategori()).thenReturn(livedata)

        val actualKategori = produkViewModel.filterByKategori().value

        verify(productUseCase).executeFilterByKategori()
        assertNull(actualKategori)
        assertNull(actualKategori?.get(1)?.namaKategori)
    }

    private val dummyProduk = arrayListOf(
        ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
        ProdukModel(namaProduk = "Produk B", hargaProduk = 20000),
        ProdukModel(namaProduk = "Produk C", hargaProduk = 11000),
        ProdukModel(namaProduk = "Produk D", hargaProduk = 21000)
    )

    @Test
    fun `when searchProduk is called, should not null and return queries`() {
        val searchText = "100"
        val searchResult = produkViewModel.searchProduk(dummyProduk, searchText)

        assertNotNull(searchResult)
        assertEquals(3, searchResult.size)
    }

    @Test
    fun `when searchProduk is called, should not null and return matching query`() {
        val searchText = "Produk A"
        val searchResult = produkViewModel.searchProduk(dummyProduk, searchText)

        assertNotNull(searchResult)
        assertEquals(1, searchResult.size)
        assertEquals(searchText, searchResult[0].namaProduk)
    }

    @Test
    fun `when searchProduk is called, should not null and return no matching query`() {
        val searchText = "Product"
        val searchResult = produkViewModel.searchProduk(dummyProduk, searchText)

        assertNotNull(searchResult)
        assertEquals(0, searchResult.size)
    }
}