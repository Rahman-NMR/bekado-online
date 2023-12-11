package com.example.testnew.model

import android.os.Parcel
import android.os.Parcelable

data class KeranjangModel(
    var diPilih: Boolean = false,
    val jumlahProduk: Long? = 0,
    val timestamp: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (diPilih) 1 else 0)
        parcel.writeValue(jumlahProduk)
        parcel.writeString(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KeranjangModel> {
        override fun createFromParcel(parcel: Parcel): KeranjangModel {
            return KeranjangModel(parcel)
        }

        override fun newArray(size: Int): Array<KeranjangModel?> {
            return arrayOfNulls(size)
        }
    }
}