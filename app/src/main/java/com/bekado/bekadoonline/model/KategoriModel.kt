package com.bekado.bekadoonline.model

import android.os.Parcel
import android.os.Parcelable

data class KategoriModel(
    val idKategori: String? = "",
    val namaKategori: String? = "",
    var posisi: Long = 0,
    val visibilitas: Boolean = false,
    val jumlahProduk: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        parcel.writeLong(posisi)
        parcel.writeByte(if (visibilitas) 1 else 0)
        parcel.writeLong(jumlahProduk)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KategoriModel> {
        override fun createFromParcel(parcel: Parcel): KategoriModel {
            return KategoriModel(parcel)
        }

        override fun newArray(size: Int): Array<KategoriModel?> {
            return arrayOfNulls(size)
        }
    }
}
