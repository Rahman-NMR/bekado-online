package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
class DetailTransaksiAdminViewModelTest {
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
    fun `when getDataAkunOwner is called, should not null and return data`() {
        val dummyAkun = AkunModel(
            "test@mail.com",
            "https://test.img/image/tester.png",
            "tester",
            "123456789",
            false,
            "123"
        )
        val livedata = MutableLiveData<AkunModel>()
        livedata.value = dummyAkun

        `when`(trxUseCase.executeGetDataAkunOwner()).thenReturn(livedata)

        val actualAkun = detailTransaksiViewModel.getDataAkunOwner().value

        verify(trxUseCase).executeGetDataAkunOwner()
        assertNotNull(actualAkun)
        assertEquals(dummyAkun, actualAkun)
    }

    @Test
    fun `when getDataAkunOwner is called, should null and return null`() {
        val livedata = MutableLiveData<AkunModel>()
        livedata.value = null

        `when`(trxUseCase.executeGetDataAkunOwner()).thenReturn(livedata)

        val actualAkun = detailTransaksiViewModel.getDataAkunOwner().value

        verify(trxUseCase).executeGetDataAkunOwner()
        assertNull(actualAkun)
        assertNull(null, actualAkun?.uid)
    }

    @Test
    fun `when updateStatusPesanan is called, response should be invoked`() {
        val path = "database/path"
        val selectedStatus = "test status"
        val selectedParent = "test parent"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(3)
            responseCallback(true)
            null
        }.`when`(trxUseCase).executeUpdateStatusPesanan(path, selectedStatus, selectedParent, response)

        detailTransaksiViewModel.updateStatusPesanan(path, selectedStatus, selectedParent, response)

        verify(response).invoke(true)
        verify(trxUseCase).executeUpdateStatusPesanan(path, selectedStatus, selectedParent, response)
    }
}