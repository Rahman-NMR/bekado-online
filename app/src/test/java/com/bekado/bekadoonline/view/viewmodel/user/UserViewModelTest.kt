package com.bekado.bekadoonline.view.viewmodel.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.bekado.bekadoonline.view.viewmodel.getOrAwaitValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var useCase: UserUseCase

    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userViewModel = UserViewModel(useCase)
    }

    @Test
    fun `test getDataAkun success`() {
        val dummyAkun = AkunModel(
            "test@mail.com",
            "https://test.img/image/tester.png",
            "tester",
            "123456789",
            false,
            "123"
        )
        val livedata = MutableLiveData<AkunModel>()
        livedata.value = dummyAkun

        `when`(useCase.executeGetDataAkun()).thenReturn(livedata)

        val actualAkun = userViewModel.getDataAkun().value
        Mockito.verify(useCase).executeGetDataAkun()
        Mockito.verifyNoMoreInteractions(useCase)

        Assert.assertEquals(dummyAkun.uid, actualAkun?.uid ?: "")
    }

    @Test
    fun `test isLoading success`() {
        val livedata = MutableLiveData<Boolean>()
        livedata.value = false
        `when`(useCase.executeLoading()).thenReturn(livedata)

        val actualLoading = userViewModel.isLoading().getOrAwaitValue()
        Mockito.verify(useCase).executeLoading()
        Assert.assertFalse(actualLoading)
    }

    @Test
    fun `test getVerified success`() {
        val livedata = MutableLiveData<VerificationResult>()
        livedata.value = VerificationResult(isGoogleVerified = true, isEmailVerified = true)

        `when`(useCase.executeIsVerified()).thenReturn(livedata.value)

        val actualVerification = userViewModel.isVerified()
        Mockito.verify(useCase).executeIsVerified()
        Assert.assertTrue(actualVerification.isGoogleVerified == true)
        Assert.assertTrue(actualVerification.isEmailVerified == true)
    }

    @Test
    fun `test logout success`() {
        userViewModel.clearAkunData()
        Mockito.verify(useCase).executeLogout()
    }
}