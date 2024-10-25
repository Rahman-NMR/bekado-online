package com.bekado.bekadoonline.di

import android.content.Context
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.repository.TrxRepositoryImpl
import com.bekado.bekadoonline.data.repository.UserRepositoryImpl
import com.bekado.bekadoonline.domain.interactor.TrxUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.UserUseCaseInteractor
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Injection {
    private fun clientGoogle(context: Context) = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail().build()

    fun provideRepository(context: Context): Repository {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val googleSignInClient = GoogleSignIn.getClient(context, clientGoogle(context))
        return Repository.getInstance(auth, db, googleSignInClient)
    }

    private fun provideUserRepository(context: Context): UserRepository {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val gsiClient = GoogleSignIn.getClient(context, clientGoogle(context))
        return UserRepositoryImpl(auth, db, gsiClient)
    }

    private fun provideTrxRepository(): TrxRepository {
        val db = FirebaseDatabase.getInstance()
        return TrxRepositoryImpl(db)
    }

    fun provideUserUseCase(context: Context): UserUseCaseInteractor {
        val akunRepository = provideUserRepository(context)
        return UserUseCaseInteractor(akunRepository)
    }

    fun provideTrxUseCase(): TrxUseCaseInteractor {
        val trxRepository = provideTrxRepository()
        return TrxUseCaseInteractor(trxRepository)
    }
}