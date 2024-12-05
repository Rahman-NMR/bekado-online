package com.bekado.bekadoonline.view.viewmodel.user

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AlamatViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userUsecase: UserUseCase

    private lateinit var alamatViewModel: AlamatViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        alamatViewModel = AlamatViewModel(userUsecase)
    }

    @Test
    fun `test getDataAlamat success`() {
        val alamatModel = Mockito.mock(AlamatModel::class.java)
        val livedata = MutableLiveData<AlamatModel>()
        livedata.value = alamatModel

        Mockito.`when`(userUsecase.executeGetDataAlamat()).thenReturn(livedata)

        val actualAlamat = alamatViewModel.getDataAlamat().value

        Mockito.verify(userUsecase).executeGetDataAlamat()
        Mockito.verifyNoMoreInteractions(userUsecase)
        Assert.assertEquals(alamatModel, actualAlamat)
    }

    @Test
    fun `test saveLatLong success`() {
        val location = Mockito.mock(Location::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(userUsecase).executeSaveLatLong(location, response)

        alamatViewModel.saveLatLong(location, response)

        Mockito.verify(userUsecase).executeSaveLatLong(location, response)
    }

    @Test
    fun `test updateDataAlamat success`() {
        val namaAlamat = "Test Alamat"
        val nohpAlamat = "1234567890"
        val alamatLengkap = "Test Alamat Lengkap"
        val kodePos = "12345"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(4)
            responseCallback(true)
            null
        }.`when`(userUsecase).executeUpdateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)

        alamatViewModel.updateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)

        Mockito.verify(userUsecase).executeUpdateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)
    }
}