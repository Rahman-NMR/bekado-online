package com.bekado.bekadoonline.data.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.model.AkunModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class AkunViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AkunViewModel
    private lateinit var repository: Repository

    private val mockAuth = mock(FirebaseAuth::class.java)
    private val mockDatabase = mock(FirebaseDatabase::class.java)
    private val mockGoogleSignInClient = mock(GoogleSignInClient::class.java)
    private val mockUser = mock(FirebaseUser::class.java)
    private val mockDatabaseRef = mock(DatabaseReference::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        repository = Repository(mockAuth, mockDatabase, mockGoogleSignInClient)
        viewModel = AkunViewModel(repository)

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("testUserId")

        `when`(mockDatabase.getReference(anyString())).thenReturn(mockDatabaseRef)
    }

    @Test
    fun testLoadCurrentUser() {
        val observer = mock(Observer::class.java) as Observer<FirebaseUser?>

        viewModel.currentUser.observeForever(observer)

        viewModel.loadCurrentUser()

        verify(observer).onChanged(mockUser)
    }

    @Test
    fun testLoadAkunData() {
        val observer = mock(Observer::class.java) as Observer<AkunModel?>
        viewModel.akunModel.observeForever(observer)

        viewModel.loadAkunData()

        verify(mockDatabaseRef).removeEventListener(any(ValueEventListener::class.java))
        verify(mockDatabaseRef).addValueEventListener(any(ValueEventListener::class.java))
    }
}