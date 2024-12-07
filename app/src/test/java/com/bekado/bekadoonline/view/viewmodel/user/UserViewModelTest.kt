package com.bekado.bekadoonline.view.viewmodel.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
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
    fun `when getDataAkun is called, should not null and return data`() {
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

        verify(useCase).executeGetDataAkun()
        assertNotNull(actualAkun)
        assertEquals(dummyAkun, actualAkun)
    }

    @Test
    fun `when getDataAkun is called, should null and return null`() {
        val livedata = MutableLiveData<AkunModel>()
        livedata.value = null

        `when`(useCase.executeGetDataAkun()).thenReturn(livedata)

        val actualAkun = userViewModel.getDataAkun().value

        verify(useCase).executeGetDataAkun()
        assertNull(actualAkun)
        assertNull(actualAkun?.uid)
    }

    @Test
    fun `when getVerified is called, verification should be returned`() {
        val dummyVerification = VerificationResult(isGoogleVerified = true, isEmailVerified = true)
        val livedata = MutableLiveData<VerificationResult>()
        livedata.value = dummyVerification

        `when`(useCase.executeIsVerified()).thenReturn(livedata.value)

        val actualVerification = userViewModel.isVerified()

        verify(useCase).executeIsVerified()
        assertNotNull(actualVerification)
        assertEquals(dummyVerification, actualVerification)
    }

    @Test
    fun `when clearAkunData is called, should clear akun data`() {
        userViewModel.clearAkunData()

        verify(useCase).executeLogout()
    }
}