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
    fun `test getDataStore success`() {
        val dummyData = TokoModel()
        val callback = Mockito.mock<(TokoModel, Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(TokoModel, Boolean) -> Unit>(0)
            responseCallback(dummyData, true)
            null
        }.`when`(repository).getDataToko(callback)

        viewModel.getDataToko(callback)

        Mockito.verify(callback).invoke(dummyData, true)
        Mockito.verify(repository).getDataToko(callback)
    }
}