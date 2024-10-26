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

    private fun auth() = FirebaseAuth.getInstance()
    private fun db() = FirebaseDatabase.getInstance()
    private fun gsiClient(context: Context) = GoogleSignIn.getClient(context, clientGoogle(context))

    fun provideRepository(context: Context): Repository {
        return Repository.getInstance(auth(), db(), gsiClient(context))
    }

    private fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(auth(), db(), gsiClient(context))
    }

    private fun provideTrxRepository(): TrxRepository {
        return TrxRepositoryImpl(db())
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