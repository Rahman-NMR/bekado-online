package com.bekado.bekadoonline.di

import android.content.Context
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.repository.AddressRepositoryImpl
import com.bekado.bekadoonline.data.repository.CartRepositoryImpl
import com.bekado.bekadoonline.data.repository.KategoriListRepositoryImpl
import com.bekado.bekadoonline.data.repository.KategoriRepositoryImpl
import com.bekado.bekadoonline.data.repository.ProductRepositoryImpl
import com.bekado.bekadoonline.data.repository.ProdukListRepositoryImpl
import com.bekado.bekadoonline.data.repository.ProdukRepositoryImpl
import com.bekado.bekadoonline.data.repository.TrxDetailRepositoryImpl
import com.bekado.bekadoonline.data.repository.TrxRepositoryImpl
import com.bekado.bekadoonline.data.repository.UserRepositoryImpl
import com.bekado.bekadoonline.data.repository.UserUpdateRepositoryImpl
import com.bekado.bekadoonline.domain.interactor.AdminUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.CartUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.ProductUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.TrxUseCaseInteractor
import com.bekado.bekadoonline.domain.interactor.UserUseCaseInteractor
import com.bekado.bekadoonline.domain.repositories.AddressRepository
import com.bekado.bekadoonline.domain.repositories.CartRepository
import com.bekado.bekadoonline.domain.repositories.KategoriListRepository
import com.bekado.bekadoonline.domain.repositories.KategoriRepository
import com.bekado.bekadoonline.domain.repositories.ProductRepository
import com.bekado.bekadoonline.domain.repositories.ProdukListRepository
import com.bekado.bekadoonline.domain.repositories.ProdukRepository
import com.bekado.bekadoonline.domain.repositories.TrxDetailRepository
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.domain.repositories.UserUpdateRepository
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

    private fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(auth(), db(), gsiClient(context))
    }

    private fun provideUserUpdateRepository(): UserUpdateRepository {
        return UserUpdateRepositoryImpl(auth(), db(), storage())
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

    private fun provideProductRepository(): ProductRepository {
        return ProductRepositoryImpl(db())
    }

    private fun provideKategoriListRepository(): KategoriListRepository {
        return KategoriListRepositoryImpl(db())
    }

    private fun provideKategoriRepository(): KategoriRepository {
        return KategoriRepositoryImpl(db(), storage())
    }

    private fun provideProdukListRepository(): ProdukListRepository {
        return ProdukListRepositoryImpl(db())
    }

    private fun provideProdukRepository(): ProdukRepository {
        return ProdukRepositoryImpl(db(), storage())
    }

    fun provideUserUseCase(context: Context): UserUseCaseInteractor {
        val akun = provideUserRepository(context)
        val alamat = provideAddressRepository()
        val userUpdate = provideUserUpdateRepository()
        return UserUseCaseInteractor(akun, alamat, userUpdate)
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

    fun provideProductUseCase(): ProductUseCaseInteractor {
        val product = provideProductRepository()
        return ProductUseCaseInteractor(product)
    }

    fun provideAdminUseCase(): AdminUseCaseInteractor {
        val kategoriList = provideKategoriListRepository()
        val kategori = provideKategoriRepository()
        val produkList = provideProdukListRepository()
        val produk = provideProdukRepository()
        return AdminUseCaseInteractor(
            kategoriListRepository = kategoriList,
            produkListRepository = produkList,
            kategoriRepository = kategori,
            produkRepository = produk
        )
    }
}