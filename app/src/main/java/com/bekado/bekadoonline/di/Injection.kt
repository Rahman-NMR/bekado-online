package com.bekado.bekadoonline.di

import android.content.Context
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.repository.UserRepositoryImpl
import com.bekado.bekadoonline.data.repository.TrxRepositoryImpl
import com.bekado.bekadoonline.domain.interactor.UserUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.TrxUseCaseInteractor
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.domain.repositories.TrxRepository
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

    private fun provideUserRepository(context: Context): UserRepository {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val gsiClient = GoogleSignIn.getClient(context, HelperAuth.clientGoogle(context))
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