package com.bekado.bekadoonline.data.model

import android.os.Parcel
import android.os.Parcelable

data class TransaksiModel(
    val idTransaksi: String? = "",
    val noPesanan: String? = "",
    val timestamp: String? = "",
    val statusPesanan: String? = "",
    val fotoProduk: String? = "",
    val namaProduk: String? = "",
    val jumlahProduk: Long? = 0,
    val currency: String? = "",
    val totalBelanja: Long? = 0,
    val produkLainnya: Long? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idTransaksi)
        parcel.writeString(noPesanan)
        parcel.writeString(timestamp)
        parcel.writeString(statusPesanan)
        parcel.writeString(fotoProduk)
        parcel.writeString(namaProduk)
        parcel.writeValue(jumlahProduk)
        parcel.writeString(currency)
        parcel.writeValue(totalBelanja)
        parcel.writeValue(produkLainnya)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransaksiModel> {
        override fun createFromParcel(parcel: Parcel): TransaksiModel {
            return TransaksiModel(parcel)
        }

        override fun newArray(size: Int): Array<TransaksiModel?> {
            return arrayOfNulls(size)
        }
    }
}