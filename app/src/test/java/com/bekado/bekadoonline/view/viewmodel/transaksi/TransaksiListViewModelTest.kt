package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TransaksiListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var trxUseCase: TrxUseCase

    private lateinit var viewModel: TransaksiListViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = TransaksiListViewModel(trxUseCase)
    }

    @Test
    fun `when getDataTransaksi is called, should not null and return data`() {
        val akunModel = AkunModel()
        val dataDummy = arrayListOf(
            TrxListModel(idTransaksi = "123"),
            TrxListModel(idTransaksi = "456"),
            TrxListModel(idTransaksi = "789")
        )
        val livedata = MutableLiveData<ArrayList<TrxListModel>?>()
        livedata.value = dataDummy

        `when`(trxUseCase.executeListDataTransaksi(akunModel)).thenReturn(livedata)

        val actualTransaksi = viewModel.getDataTransaksi(akunModel).value

        verify(trxUseCase).executeListDataTransaksi(akunModel)
        assertNotNull(actualTransaksi)
        assertEquals(3, actualTransaksi?.size)
        assertEquals(456, actualTransaksi?.get(1)?.idTransaksi?.toInt())
    }

    @Test
    fun `when getDataTransaksi is called, should null and return null`() {
        val akunModel = AkunModel()
        val livedata = MutableLiveData<ArrayList<TrxListModel>?>()
        livedata.value = null

        `when`(trxUseCase.executeListDataTransaksi(akunModel)).thenReturn(livedata)

        val actualTransaksi = viewModel.getDataTransaksi(akunModel).value

        verify(trxUseCase).executeListDataTransaksi(akunModel)
        assertNull(actualTransaksi)
        assertNull(actualTransaksi?.size)
    }

    private val dummyTransaksi = arrayListOf(
        TrxListModel(noPesanan = "1234"),
        TrxListModel(noPesanan = "4567"),
        TrxListModel(noPesanan = "7890"),
        TrxListModel(noPesanan = "12345")
    )

    @Test
    fun `when searchTransaksi is called, should not null and return queries`() {
        val searchText = "4"
        val searchResult = viewModel.searchTransaksi(dummyTransaksi, searchText)

        assertNotNull(searchResult)
        assertEquals(3, searchResult.size)
    }

    @Test
    fun `when searchTransaksi is called, should not null and return matching query`() {
        val searchText = "7890"
        val searchResult = viewModel.searchTransaksi(dummyTransaksi, searchText)

        assertNotNull(searchResult)
        assertEquals(1, searchResult.size)
        assertEquals(searchText, searchResult[0].noPesanan)
    }

    @Test
    fun `when searchTransaksi is called, should not null and return no matching query`() {
        val searchText = "456754"
        val searchResult = viewModel.searchTransaksi(dummyTransaksi, searchText)

        assertNotNull(searchResult)
        assertEquals(0, searchResult.size)
    }
}