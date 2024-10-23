package com.bekado.bekadoonline.di

import android.content.Context
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.repository.AkunRepositoryImpl
import com.bekado.bekadoonline.domain.interactor.AkunUseCaseInteractor
import com.bekado.bekadoonline.domain.repositories.AkunRepository
import com.bekado.bekadoonline.helper.HelperAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Injection {
    fun provideRepository(context: Context): Repository {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val googleSignInClient = GoogleSignIn.getClient(context, HelperAuth.clientGoogle(context))
        return Repository.getInstance(auth, db, googleSignInClient)
    }

    private fun provideAkunRepository(context: Context): AkunRepository {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val gsiClient = GoogleSignIn.getClient(context, HelperAuth.clientGoogle(context))
        return AkunRepositoryImpl(auth, db, gsiClient)
    }

    fun provideAkunUseCase(context: Context): AkunUseCaseInteractor {
        val akunRepository = provideAkunRepository(context)
        return AkunUseCaseInteractor(akunRepository)
    }
}