package com.bekado.bekadoonline.view.viewmodel.user

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.google.firebase.auth.AuthCredential
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

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
    fun `when loginAuthManual is called, response should be invoked`() {
        val email = "test@mail.com"
        val password = "password123"
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(2)
            responseCallback(true)
            null
        }.`when`(useCase).executeLoginAuthManual(email, password, response)

        authViewModel.loginAuthManual(email, password, response)

        verify(useCase).executeLoginAuthManual(email, password, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when loginAuthWithGoogle is called, response should be invoked`() {
        val data = Mockito.mock(Intent::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(useCase).executeLoginAuthWithGoogle(data, response)

        authViewModel.loginAuthWithGoogle(data, response)

        verify(useCase).executeLoginAuthWithGoogle(data, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when registerAuth is called, response should be invoked`() {
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

        verify(useCase).executeRegisterAuth(email, password, nama, noHp, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when linkToGoogle is called, response should be invoked`() {
        val data = Mockito.mock(Intent::class.java)
        val response = Mockito.mock<(Boolean, String) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            responseCallback(true, "success")
            null
        }.`when`(useCase).executeLinkToGoogle(data, response)

        authViewModel.linkToGoogle(data, response)

        verify(useCase).executeLinkToGoogle(data, response)
        verify(response).invoke(true, "success")
    }

    @Test
    fun `when linkCredentials is called, response should be invoked`() {
        val credential = Mockito.mock(AuthCredential::class.java)
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(useCase).executeLinkCredentials(credential, response)

        authViewModel.linkCredentials(credential, response)

        verify(useCase).executeLinkCredentials(credential, response)
        verify(response).invoke(true)
    }

    @Test
    fun `when reAuthenticate is called, response should be invoked`() {
        val currentPassword = "password123"
        val inputNotEmpty = Mockito.mock<(Boolean) -> Unit>()
        val response = Mockito.mock<(Boolean, Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean, Boolean) -> Unit>(1)
            responseCallback(true, true)
            null
        }.`when`(useCase).executeReAuthenticate(currentPassword, response)

        authViewModel.reAuthenticate(currentPassword, inputNotEmpty, response)

        verify(useCase).executeReAuthenticate(currentPassword, response)
        verify(response).invoke(true, true)
    }

    @Test
    fun `when ubahPassword is called, response should be invoked`() {
        val newPassword = "newPassword123"
        val inputNotEmpty = Mockito.mock<(Boolean) -> Unit>()
        val response = Mockito.mock<(Boolean) -> Unit>()

        Mockito.doAnswer { invocation ->
            val responseCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            responseCallback(true)
            null
        }.`when`(useCase).executeUpdatePassword(newPassword, response)

        authViewModel.updatePassword(newPassword, inputNotEmpty, response)

        verify(useCase).executeUpdatePassword(newPassword, response)
        verify(response).invoke(true)
    }
}