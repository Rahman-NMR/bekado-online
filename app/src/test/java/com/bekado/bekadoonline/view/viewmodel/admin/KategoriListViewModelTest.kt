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
class KategoriListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var adminUseCase: AdminUseCase

    private lateinit var kategoriListViewModel: KategoriListViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        kategoriListViewModel = KategoriListViewModel(adminUseCase)
    }

    @Test
    fun `when getKategoriList is called, should not null and return data`() {
        val dummyKategoriList = arrayListOf(
            KategoriModel(idKategori = "1", namaKategori = "Makanan", posisi = 1, visibilitas = true),
            KategoriModel(idKategori = "2", namaKategori = "Minuman", posisi = 2, visibilitas = true),
            KategoriModel(idKategori = "3", namaKategori = "Cemilan", posisi = 3, visibilitas = false)
        )
        val livedata = MutableLiveData<ArrayList<KategoriModel>>()
        livedata.value = dummyKategoriList

        `when`(adminUseCase.executeGetDataKategoriList()).thenReturn(livedata)

        val actualKategoriList = kategoriListViewModel.getKategoriList().value

        verify(adminUseCase).executeGetDataKategoriList()
        assertNotNull(actualKategoriList)
        assertEquals(3, actualKategoriList?.size)
        assertEquals(dummyKategoriList, actualKategoriList)
    }

    @Test
    fun `when getKategoriList is called, should null and return null`() {
        val livedata = MutableLiveData<ArrayList<KategoriModel>>()
        livedata.value = null

        `when`(adminUseCase.executeGetDataKategoriList()).thenReturn(livedata)

        val actualKategoriList = kategoriListViewModel.getKategoriList().value

        verify(adminUseCase).executeGetDataKategoriList()
        assertNull(actualKategoriList)
        assertNull(actualKategoriList?.get(0)?.idKategori)
    }

    @Test
    fun `when addKategori is called, response should be invoked`() {
        val namaKategori = "Makanan"
        val posisi = 1L
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeAddNewKategori(namaKategori, posisi, response)

        kategoriListViewModel.addKategori(namaKategori, posisi, response)

        verify(adminUseCase).executeAddNewKategori(namaKategori, posisi, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when updateNamaKategori is called, response should be invoked`() {
        val idKategori = "1"
        val namaKategori = "Makanan"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeUpdateNamaKategori(idKategori, namaKategori, response)

        kategoriListViewModel.updateNamaKategori(idKategori, namaKategori, response)

        verify(adminUseCase).executeUpdateNamaKategori(idKategori, namaKategori, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when updateVisibilitasKategori is called, response should be invoked`() {
        val idKategori = "1"
        val visibilitas = true
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(adminUseCase).executeUpdateVisibilitasKategori(idKategori, visibilitas, response)

        kategoriListViewModel.updateVisibilitasKategori(idKategori, visibilitas, response)

        verify(adminUseCase).executeUpdateVisibilitasKategori(idKategori, visibilitas, response)
        verify(response).invoke(true)
    }
}