package com.bekado.bekadoonline.view.viewmodel.user

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase
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
    fun `when getDataAlamat is called, should not null and return data`() {
        val dummyAlamat = AlamatModel(
            nama = "Test Alamat",
            noHp = "1234567890",
            alamatLengkap = "Test Alamat Lengkap",
            kodePos = "12345",
            latitude = "0.0",
            longitude = "0.0"
        )
        val livedata = MutableLiveData<AlamatModel>()
        livedata.value = dummyAlamat

        `when`(userUsecase.executeGetDataAlamat()).thenReturn(livedata)

        val actualAlamat = alamatViewModel.getDataAlamat().value

        verify(userUsecase).executeGetDataAlamat()
        assertNotNull(actualAlamat)
        assertEquals(dummyAlamat, actualAlamat)
    }

    @Test
    fun `when getDataAlamat is called, should null and return null`() {
        val livedata = MutableLiveData<AlamatModel>()
        livedata.value = null

        `when`(userUsecase.executeGetDataAlamat()).thenReturn(livedata)

        val actualAlamat = alamatViewModel.getDataAlamat().value

        verify(userUsecase).executeGetDataAlamat()
        assertNull(actualAlamat)
        assertNull(actualAlamat?.nama)
    }

    @Test
    fun `when saveLatLong is called, response should be invoked`() {
        val location = Mockito.mock(Location::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(userUsecase).executeSaveLatLong(location, response)

        alamatViewModel.saveLatLong(location, response)

        verify(userUsecase).executeSaveLatLong(location, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when updateDataAlamat is called, response should be invoked`() {
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

        verify(userUsecase).executeUpdateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)
        verify(response).invoke(true)
    }
}