package com.bekado.bekadoonline.view.viewmodel.transaksi

import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class CheckoutViewModelTest {
    @Mock
    private lateinit var trxUseCase: TrxUseCase

    private lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = CheckoutViewModel(trxUseCase)
    }

    @Test
    fun `when addNewTransaksi is called, response should be invoked`() {
        val dataTransaksi = TrxDetailModel()
        val dataAlamat = AlamatModel()
        val produkMap = mutableMapOf<String, Any>()
        val totalBelanja = 100L
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer {
            val responseCallback = it.getArgument<(Boolean) -> Unit>(4)
            responseCallback(true)
            null
        }.`when`(trxUseCase).executeCreateTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)

        viewModel.addNewTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)

        verify(trxUseCase).executeCreateTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)
        Mockito.verify(response).invoke(true)
    }

    @Test
    fun `when generateIdPesanan is called, idPesanan should be returned`() {
        val currentTime = "1234567890"
        val totalItem = 4

        val idPesanan = viewModel.generateIdPesanan(currentTime, totalItem)
        assertEquals("INV/4123/4567/890", idPesanan)
    }

    @Test
    fun `when rincianHarga is called, rincian should be returned`() {
        val dummyKeranjang = arrayListOf(
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk A", hargaProduk = 1000),
                keranjangModel = KeranjangModel(jumlahProduk = 1)
            ),
            CombinedKeranjangModel(
                produkModel = ProdukModel(namaProduk = "Produk B", hargaProduk = 2000),
                keranjangModel = KeranjangModel(jumlahProduk = 2)
            )
        )

        val rincian = viewModel.rincianHarga(dummyKeranjang, 1234)

        assertEquals(5000, rincian.totalHarga)
        assertEquals(2, rincian.totalItem)
        assertEquals(1234, rincian.ongkir)
        assertEquals(6234, rincian.totalBelanja)
    }

    @Test
    fun `when hitungJarak is called, jarakTxt should be returned`() {
        val latitude = "-7.4547115"
        val longitude = "109.258109"
        val jarakTxt = viewModel.hitungJarak(latitude, longitude)
        assertEquals("0 m", jarakTxt)
    }
}