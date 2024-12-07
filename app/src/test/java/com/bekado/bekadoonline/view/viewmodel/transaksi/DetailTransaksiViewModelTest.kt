package com.bekado.bekadoonline.view.viewmodel.transaksi

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class DetailTransaksiViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var trxUseCase: TrxUseCase

    private lateinit var detailTransaksiViewModel: DetailTransaksiViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        detailTransaksiViewModel = DetailTransaksiViewModel(trxUseCase)
    }

    @Test
    fun `when getProdukList is called, should not null and return data`() {
        val dummyProdukList = arrayListOf(
            CombinedKeranjangModel(
                produkModel = ProdukModel(
                    namaProduk = "Produk A",
                    hargaProduk = 10000,
                    fotoProduk = "foto_produk_a.jpg"
                ),
                keranjangModel = KeranjangModel(jumlahProduk = 2)
            ),
            CombinedKeranjangModel(
                produkModel = ProdukModel(
                    namaProduk = "Produk B",
                    hargaProduk = 20000,
                    fotoProduk = "foto_produk_b.jpg"
                ),
                keranjangModel = KeranjangModel(jumlahProduk = 1)
            )
        )
        val livedata = MutableLiveData<ArrayList<CombinedKeranjangModel>>()
        livedata.value = dummyProdukList

        `when`(trxUseCase.executeGetProdukList()).thenReturn(livedata)

        val actualProductList = detailTransaksiViewModel.getProdukList().value

        verify(trxUseCase).executeGetProdukList()
        assertNotNull(actualProductList)
        assertEquals(dummyProdukList, actualProductList)
        assertEquals(2, actualProductList?.size)
    }

    @Test
    fun `when getProdukList is called, should null and return null`() {
        val livedata = MutableLiveData<ArrayList<CombinedKeranjangModel>>()
        livedata.value = null

        `when`(trxUseCase.executeGetProdukList()).thenReturn(livedata)

        val actualProductList = detailTransaksiViewModel.getProdukList().value

        verify(trxUseCase).executeGetProdukList()
        assertNull(actualProductList)
    }

    @Test
    fun `when getDetailTransaksi is called, should not null and return data`() {
        val path = "database/path"
        val dummyDetail = TrxDetailModel(
            noPesanan = "INV/1234/5678/90",
            idTransaksi = "ABC123"
        )
        val livedata = MutableLiveData<TrxDetailModel>()
        livedata.value = dummyDetail

        `when`(trxUseCase.executeGetDetailTransaksi(path)).thenReturn(livedata)

        val actualDetail = detailTransaksiViewModel.getDetailTransaksi(path).value

        verify(trxUseCase).executeGetDetailTransaksi(path)
        assertNotNull(actualDetail)
        assertEquals(dummyDetail, actualDetail)
    }

    @Test
    fun `when getDetailTransaksi is called, should null and return null`() {
        val path = "database/path"
        val livedata = MutableLiveData<TrxDetailModel>()
        livedata.value = null

        `when`(trxUseCase.executeGetDetailTransaksi(path)).thenReturn(livedata)

        val actualDetail = detailTransaksiViewModel.getDetailTransaksi(path).value

        verify(trxUseCase).executeGetDetailTransaksi(path)
        assertNull(actualDetail)
    }

    @Test
    fun `when getAlamat is called, should not null and return data`() {
        val dummyAlamat = AlamatModel(
            nama = "User Name",
            noHp = "1234567890",
            alamatLengkap = "Jl. Raya, No. 123",
            kodePos = "12345",
            latitude = "12.345",
            longitude = "54.321"
        )
        val livedata = MutableLiveData<AlamatModel>()
        livedata.value = dummyAlamat

        `when`(trxUseCase.executeGetAlamat()).thenReturn(livedata)

        val actualAlamat = detailTransaksiViewModel.getAlamat().value

        verify(trxUseCase).executeGetAlamat()
        assertNotNull(actualAlamat)
        assertEquals(dummyAlamat, actualAlamat)
    }

    @Test
    fun `when getAlamat is called, should null and return null`() {
        val livedata = MutableLiveData<AlamatModel>()
        livedata.value = null

        `when`(trxUseCase.executeGetAlamat()).thenReturn(livedata)

        val actualAlamat = detailTransaksiViewModel.getAlamat().value

        verify(trxUseCase).executeGetAlamat()
        assertNull(actualAlamat)
    }

    @Test
    fun `when getPayment is called, should not null and return data`() {
        val dummyPayment = BuktiPembayaranModel(
            biayaTransfer = 10000,
            buktiTransaksi = "bukti_transaksi.jpg",
            fotoBank = "foto_bank.jpg",
            namaBank = "Bank ABC",
            noRek = "123456789",
            pemilikBank = "Pemilik Bank"
        )
        val livedata = MutableLiveData<BuktiPembayaranModel>()
        livedata.value = dummyPayment

        `when`(trxUseCase.executeGetPayment()).thenReturn(livedata)

        val actualPayment = detailTransaksiViewModel.getPayment().value

        verify(trxUseCase).executeGetPayment()
        assertNotNull(actualPayment)
        assertEquals(dummyPayment, actualPayment)
    }

    @Test
    fun `when getPayment is called, should null and return null`() {
        val livedata = MutableLiveData<BuktiPembayaranModel>()
        livedata.value = null

        `when`(trxUseCase.executeGetPayment()).thenReturn(livedata)

        val actualPayment = detailTransaksiViewModel.getPayment().value

        verify(trxUseCase).executeGetPayment()
        assertNull(actualPayment)
    }

    @Test
    fun `when uploadBuktiPembayaran is called, response should be invoked`() {
        val imageUri = Mockito.mock(Uri::class.java)
        val statusPesanan = "status"
        val response = Mockito.mock<(Boolean) -> Unit>()
        val pathDetailTrx = "database/path"

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(3)
            responseCallback(true)
            null
        }.`when`(trxUseCase).executeUploadBuktiPembayaran(imageUri, statusPesanan, pathDetailTrx, response)

        detailTransaksiViewModel.getDetailTransaksi(pathDetailTrx)
        detailTransaksiViewModel.uploadBuktiPembayaran(imageUri, statusPesanan, response)

        verify(trxUseCase).executeUploadBuktiPembayaran(imageUri, statusPesanan, pathDetailTrx, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when timestampToFormated is called, should return the correct formatted date`() {
        val actualFormattedDate = detailTransaksiViewModel.timestampToFormated(1234567890)
        assertEquals("15 January 1970, 13:56", actualFormattedDate)
    }
}