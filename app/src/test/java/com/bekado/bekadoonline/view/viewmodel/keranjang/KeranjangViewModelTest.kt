package com.bekado.bekadoonline.view.viewmodel.keranjang

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.CartUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KeranjangViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var cartUseCase: CartUseCase

    private lateinit var keranjangViewModel: KeranjangViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        keranjangViewModel = KeranjangViewModel(cartUseCase)
    }

    @Test
    fun `when getDataKeranjang is called, should not null and return data`() {
        val dataDummy = arrayListOf(
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
                keranjangModel = KeranjangModel()
            ),
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk B", hargaProduk = 20000),
                keranjangModel = KeranjangModel()
            ),
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk C", hargaProduk = 30000),
                keranjangModel = KeranjangModel()
            )
        )
        val livedata = MutableLiveData<ArrayList<CombinedKeranjangModel>?>()
        livedata.value = dataDummy

        `when`(cartUseCase.executeGetDataKeranjang()).thenReturn(livedata)

        val actualKeranjang = keranjangViewModel.getDataKeranjang().value

        verify(cartUseCase).executeGetDataKeranjang()
        assertNotNull(actualKeranjang)
        assertEquals(3, actualKeranjang?.size)
        assertEquals("Produk B", actualKeranjang?.get(1)?.produkModel?.namaProduk)
    }

    @Test
    fun `when getDataKeranjang is called, should null and return null`() {
        val livedata = MutableLiveData<ArrayList<CombinedKeranjangModel>?>()
        livedata.value = null

        `when`(cartUseCase.executeGetDataKeranjang()).thenReturn(livedata)

        val actualKeranjang = keranjangViewModel.getDataKeranjang().value

        verify(cartUseCase).executeGetDataKeranjang()
        assertNull(actualKeranjang)
        assertEquals(null, actualKeranjang?.size)
    }

    @Test
    fun `when aturJumlahProduk is called, response should be invoked`() {
        val path = "database/path"
        val isPlus = true
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(cartUseCase).executeUpdateJumlahProduk(path, isPlus, response)

        keranjangViewModel.addJumlahProduk(path, isPlus, response)

        verify(cartUseCase).executeUpdateJumlahProduk(path, isPlus, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when updateProdukTerpilih is called, response should be invoked`() {
        val idProduk = "123"
        val isChecked = true
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(cartUseCase).executeUpdateProdukTerpilih(idProduk, isChecked, response)

        keranjangViewModel.updateProdukTerpilih(idProduk, isChecked, response)

        verify(cartUseCase).executeUpdateProdukTerpilih(idProduk, isChecked, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when deleteThisProduk is called, response should be invoked`() {
        val idProduk = "123"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(cartUseCase).executeDeleteThisProduk(idProduk, response)

        keranjangViewModel.deleteThisProduk(idProduk, response)

        verify(cartUseCase).executeDeleteThisProduk(idProduk, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when cancelAction is called, response should be invoked`() {
        val itemKeranjang = CombinedKeranjangModel(
            produkModel = ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
            keranjangModel = KeranjangModel(jumlahProduk = 1)
        )

        keranjangViewModel.cancelAction(itemKeranjang)

        verify(cartUseCase).executeCancelAction(itemKeranjang)
    }

    @Test
    fun `when deleteSelectedProduk is called, response should be invoked`() {
        val produkSelected = arrayListOf(
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk A", hargaProduk = 10000),
                keranjangModel = KeranjangModel(diPilih = true)
            ),
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk B", hargaProduk = 20000),
                keranjangModel = KeranjangModel(diPilih = true)
            )
        )
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(cartUseCase).executeDeleteSelectedProduk(produkSelected, response)

        keranjangViewModel.deleteSelectedProduk(produkSelected, response)

        verify(cartUseCase).executeDeleteSelectedProduk(produkSelected, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when checkProdukExists is called, should return true and jumlahProduk not null`() {
        val idProduk = "123"
        val response = Mockito.mock<(Boolean, Long) -> Unit>()
        val jumlahProduk = 5L

        val isExistsCaptor = ArgumentCaptor.forClass(Boolean::class.java)
        val jumlahProdukCaptor = ArgumentCaptor.forClass(Long::class.java)

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean, Long) -> Unit>(1)
            responseCallback(true, jumlahProduk)
            null
        }.`when`(cartUseCase).executeProdukExistsInKeranjang(idProduk, response)

        keranjangViewModel.produkExistsInKeranjang(idProduk, response)

        verify(cartUseCase).executeProdukExistsInKeranjang(idProduk, response)
        verify(response).invoke(isExistsCaptor.capture(), jumlahProdukCaptor.capture())

        assertTrue(isExistsCaptor.value)
        assertEquals(jumlahProduk, jumlahProdukCaptor.value)
    }

    @Test
    fun `when checkProdukExists is called, should return false`() {
        val idProduk = "123"
        val response = Mockito.mock<(Boolean, Long) -> Unit>()
        val jumlahProduk = 0L

        val isExistsCaptor = ArgumentCaptor.forClass(Boolean::class.java)
        val jumlahProdukCaptor = ArgumentCaptor.forClass(Long::class.java)

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean, Long) -> Unit>(1)
            responseCallback(false, jumlahProduk)
            null
        }.`when`(cartUseCase).executeProdukExistsInKeranjang(idProduk, response)

        keranjangViewModel.produkExistsInKeranjang(idProduk, response)

        verify(cartUseCase).executeProdukExistsInKeranjang(idProduk, response)
        verify(response).invoke(isExistsCaptor.capture(), jumlahProdukCaptor.capture())

        assertFalse(isExistsCaptor.value)
        assertEquals(jumlahProduk, jumlahProdukCaptor.value)
    }

    @Test
    fun `when addDataProdukKeKeranjang is called, response should be invoked`() {
        val produk = ProdukModel(namaProduk = "Produk A", hargaProduk = 10000)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(cartUseCase).executeAddDataProdukKeKeranjang(produk, response)

        keranjangViewModel.addDataProdukKeKeranjang(produk, response)

        verify(cartUseCase).executeAddDataProdukKeKeranjang(produk, response)
        verify(response).invoke(true)
    }
}