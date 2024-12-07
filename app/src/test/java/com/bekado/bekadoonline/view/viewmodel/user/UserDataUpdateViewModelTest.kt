package com.bekado.bekadoonline.view.viewmodel.user

import android.net.Uri
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UserDataUpdateViewModelTest {
    @Mock
    private lateinit var userUsecase: UserUseCase

    private lateinit var viewModel: UserDataUpdateViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = UserDataUpdateViewModel(userUsecase)
    }

    @Test
    fun `when updateDataAkun is called, response should be invoked`() {
        val pathDb = "database/path"
        val value = "test value"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(userUsecase).executeUpdateDataAkun(pathDb, value, response)

        viewModel.updateDataAkun(pathDb, value, response)

        verify(userUsecase).executeUpdateDataAkun(pathDb, value, response)
        Mockito.verify(response).invoke(true)
    }

    @Test
    fun `when updateImageUri is called, response should be invoked`() {
        val imageUri = Mockito.mock(Uri::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(userUsecase).executeUpdateImageUri(imageUri, response)

        viewModel.updateImageUri(imageUri, response)

        Mockito.verify(userUsecase).executeUpdateImageUri(imageUri, response)
        Mockito.verify(response).invoke(true)
    }
}