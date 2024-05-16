package com.bekado.bekadoonline.data.model

import android.os.Parcel
import android.os.Parcelable

data class ProdukModel(
    val fotoProduk: String? = "",
    val namaProduk: String? = "",
    val currency: String? = "",
    val hargaProduk: Long? = 0,
    val visibility: Boolean = false,
    val idProduk: String? = "",
    val idKategori: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fotoProduk)
        parcel.writeString(namaProduk)
        parcel.writeString(currency)
        parcel.writeValue(hargaProduk)
        parcel.writeByte(if (visibility) 1 else 0)
        parcel.writeString(idProduk)
        parcel.writeString(idKategori)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProdukModel> {
        override fun createFromParcel(parcel: Parcel): ProdukModel {
            return ProdukModel(parcel)
        }

        override fun newArray(size: Int): Array<ProdukModel?> {
            return arrayOfNulls(size)
        }
    }
}