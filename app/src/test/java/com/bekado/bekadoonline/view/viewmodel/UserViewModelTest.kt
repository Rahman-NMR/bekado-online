package com.bekado.bekadoonline.view.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.usecase.AkunUseCase
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class UserViewModelTest{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserViewModel

    @Mock
    private lateinit var getAkun: AkunUseCase

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

        `when`(getAkun.execute()).thenReturn(livedata)

        val actualAkun = viewModel.getDataAkun().getOrAwaitValue()
        verify(getAkun).execute()
        verifyNoMoreInteractions(getAkun)
        if (actualAkun != null) {
            Assert.assertEquals(dummyStory.nama, actualAkun.nama)
        }
    }
}