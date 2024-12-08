package com.bekado.bekadoonline.view.viewmodel.admin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.KategoriModel
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
class KategoriViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var adminUseCase: AdminUseCase

    private lateinit var kategoriViewModel: KategoriViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        kategoriViewModel = KategoriViewModel(adminUseCase)
    }

    @Test
    fun `when getDataKategori is called, should not null and return data`() {
        val dummyKategori = KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true)
        val livedata = MutableLiveData<KategoriModel>()
        livedata.value = dummyKategori

        `when`(adminUseCase.executeGetDataKategori(dummyKategori.idKategori)).thenReturn(livedata)

        val actualKategori = kategoriViewModel.getDataKategori(dummyKategori.idKategori).value

        verify(adminUseCase).executeGetDataKategori(dummyKategori.idKategori)
        assertNotNull(actualKategori)
        assertEquals(dummyKategori, actualKategori)
    }

    @Test
    fun `when getDataKategori is called, should null and return null`() {
        val livedata = MutableLiveData<KategoriModel>()
        livedata.value = null

        `when`(adminUseCase.executeGetDataKategori("")).thenReturn(livedata)

        val actualKategori = kategoriViewModel.getDataKategori("").value

        verify(adminUseCase).executeGetDataKategori("")
        assertNull(actualKategori)
        assertNull(actualKategori?.idKategori)
    }

    @Test
    fun `when deleteKategori is called, response should be invoked`() {
        val idKategori = "1"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeDeleteKategori(idKategori, response)

        kategoriViewModel.deleteKategori(idKategori, response)

        verify(adminUseCase).executeDeleteKategori(idKategori, response)
        verify(response).invoke(true)
    }
}