package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    fun getAuthCurrentUser(): FirebaseUser?
    fun getAkun(): LiveData<AkunModel?>
    fun getLoading(): LiveData<Boolean>
    fun logoutAkun()
    fun removeListener()
}