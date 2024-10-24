package com.bekado.bekadoonline.view.viewmodel.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.bekado.bekadoonline.view.viewmodel.getOrAwaitValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class UserViewModelTest{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserViewModel

    @Mock
    private lateinit var getAkun: UserUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = UserViewModel(getAkun)
    }

    @Test
    fun testLoadAkunData() {
        val dummyStory = AkunModel(
            "qwerty@mail.com",
            "https://test.img/image/tester.png",
            "Tester",
            "123",
            false,
            "312312"
        )
        val livedata = MutableLiveData<AkunModel>()
        livedata.value = dummyStory

        Mockito.`when`(getAkun.execute()).thenReturn(livedata)

        val actualAkun = viewModel.getDataAkun().getOrAwaitValue()
        Mockito.verify(getAkun).execute()
        Mockito.verifyNoMoreInteractions(getAkun)
        if (actualAkun != null) {
            Assert.assertEquals(dummyStory.nama, actualAkun.nama)
        }
    }
}