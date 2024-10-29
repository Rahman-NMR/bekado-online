package com.bekado.bekadoonline.di

import android.content.Context
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.repository.AddressRepositoryImpl
import com.bekado.bekadoonline.data.repository.CartRepositoryImpl
import com.bekado.bekadoonline.data.repository.TrxDetailRepositoryImpl
import com.bekado.bekadoonline.data.repository.TrxRepositoryImpl
import com.bekado.bekadoonline.data.repository.UserRepositoryImpl
import com.bekado.bekadoonline.domain.interactor.CartUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.TrxUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.UserUseCaseInteractor
import com.bekado.bekadoonline.domain.repositories.AddressRepository
import com.bekado.bekadoonline.domain.repositories.CartRepository
import com.bekado.bekadoonline.domain.repositories.TrxDetailRepository
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object Injection {
    private fun clientGoogle(context: Context) = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail().build()

    private fun auth() = FirebaseAuth.getInstance()
    private fun db() = FirebaseDatabase.getInstance()
    private fun storage() = FirebaseStorage.getInstance()
    private fun gsiClient(context: Context) = GoogleSignIn.getClient(context, clientGoogle(context))

    fun provideRepository(context: Context): Repository { //todo : delete this after complete all
        return Repository.getInstance(auth(), db(), gsiClient(context))
    }

    private fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(auth(), db(), gsiClient(context))
    }

    private fun provideAddressRepository(): AddressRepository {
        return AddressRepositoryImpl(auth(), db())
    }

    private fun provideTrxRepository(): TrxRepository {
        return TrxRepositoryImpl(auth(), db())
    }

    private fun provideTrxDetailRepository(): TrxDetailRepository {
        return TrxDetailRepositoryImpl(db(), storage())
    }

    private fun provideCartRepository(): CartRepository {
        return CartRepositoryImpl(auth(), db())
    }

    fun provideUserUseCase(context: Context): UserUseCaseInteractor {
        val akunRepository = provideUserRepository(context)
        val alamatRepository = provideAddressRepository()
        return UserUseCaseInteractor(akunRepository, alamatRepository)
    }

    fun provideTrxUseCase(): TrxUseCaseInteractor {
        val transaksi = provideTrxRepository()
        val detail = provideTrxDetailRepository()
        return TrxUseCaseInteractor(transaksi, detail)
    }

    fun provideCartUseCase(): CartUseCaseInteractor {
        val cartRepository = provideCartRepository()
        return CartUseCaseInteractor(cartRepository)
    }
}