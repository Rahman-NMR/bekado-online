package com.bekado.bekadoonline.view.viewmodel.others

import com.bekado.bekadoonline.data.model.TokoModel
import com.bekado.bekadoonline.data.repository.ZZZSimpleRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class AboutBekadoViewModelTest {
    @Mock
    private lateinit var repository: ZZZSimpleRepository

    private lateinit var viewModel: AboutBekadoViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = AboutBekadoViewModel(repository)
    }

    @Test
    fun `when getDataStore is called, response should be invoked`() {
        val dummyData = TokoModel(
            nama = "Test Toko",
            alamat = "Test Alamat",
            kontak = "Test Kontak",
            operasional = "Test Operasional",
            foto = "Test Foto"
        )
        val response = Mockito.mock<(TokoModel, Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(TokoModel, Boolean) -> Unit>(0)
            responseCallback(dummyData, true)
            null
        }.`when`(repository).getDataToko(response)

        viewModel.getDataToko(response)

        verify(repository).getDataToko(response)
        verify(response).invoke(dummyData, true)
    }
}