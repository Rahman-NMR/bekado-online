package com.bekado.bekadoonline.view.viewmodel.user

import android.content.Intent
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
class AuthViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var useCase: UserUseCase

    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authViewModel = AuthViewModel(useCase)
    }

    @Test
    fun `test loginAuthManual success`() {
        val email = "test@mail.com"
        val password = "password123"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(false)
            null
        }.`when`(useCase).executeLoginAuthManual(email, password, response)

        authViewModel.loginAuthManual(email, password, response)

        Mockito.verify(response).invoke(false)
        Mockito.verify(useCase).executeLoginAuthManual(email, password, response)
    }

    @Test
    fun `test loginAuthWithGoogle success`() {
        val data = Mockito.mock(Intent::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(useCase).executeLoginAuthWithGoogle(data, response)

        authViewModel.loginAuthWithGoogle(data, response)

        Mockito.verify(response).invoke(true)
        Mockito.verify(useCase).executeLoginAuthWithGoogle(data, response)
    }

    @Test
    fun `test registerAuth success`() {
        val email = "test@mail.com"
        val password = "password123"
        val nama = "Test User"
        val noHp = "1234567890"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(4)
            responseCallback(true)
            null
        }.`when`(useCase).executeRegisterAuth(email, password, nama, noHp, response)

        authViewModel.registerAuth(email, password, nama, noHp, response)

        Mockito.verify(response).invoke(true)
        Mockito.verify(useCase).executeRegisterAuth(email, password, nama, noHp, response)
    }
}