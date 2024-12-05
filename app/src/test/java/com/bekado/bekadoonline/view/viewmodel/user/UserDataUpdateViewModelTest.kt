package com.bekado.bekadoonline.view.viewmodel.user

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserDataUpdateViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userUsecase: UserUseCase

    private lateinit var viewModel: UserDataUpdateViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = UserDataUpdateViewModel(userUsecase)
    }

    @Test
    fun `test updateDataAkun success`() {
        val pathDb = "testPath"
        val value = "testValue"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(false)
            null
        }.`when`(userUsecase).executeUpdateDataAkun(pathDb, value, response)

        viewModel.updateDataAkun(pathDb, value, response)

        Mockito.verify(userUsecase).executeUpdateDataAkun(pathDb, value, response)
    }

    @Test
    fun `test updateImageUri success`() {
        val imageUri = Mockito.mock(Uri::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(false)
            null
        }.`when`(userUsecase).executeUpdateImageUri(imageUri, response)

        viewModel.updateImageUri(imageUri, response)

        Mockito.verify(userUsecase).executeUpdateImageUri(imageUri, response)
    }
}