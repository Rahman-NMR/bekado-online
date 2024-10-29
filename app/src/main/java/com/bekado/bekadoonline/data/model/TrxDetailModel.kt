package com.bekado.bekadoonline.data.model

import android.os.Parcel
import android.os.Parcelable

data class TrxDetailModel(
    val currency: String? = "",
    val idTransaksi: String? = "",
    val metodePembayaran: String? = "",
    val noPesanan: String? = "",
    val ongkir: Long? = 0,
    val parentStatus: String? = "",
    val statusPesanan: String? = "",
    val timestamp: String? = "",
    val totalBelanja: Long? = 0,
    val totalHarga: Long? = 0,
    val totalItem: Long? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(currency)
        parcel.writeString(idTransaksi)
        parcel.writeString(metodePembayaran)
        parcel.writeString(noPesanan)
        parcel.writeValue(ongkir)
        parcel.writeString(parentStatus)
        parcel.writeString(statusPesanan)
        parcel.writeString(timestamp)
        parcel.writeValue(totalBelanja)
        parcel.writeValue(totalHarga)
        parcel.writeValue(totalItem)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrxDetailModel> {
        override fun createFromParcel(parcel: Parcel): TrxDetailModel {
            return TrxDetailModel(parcel)
        }

        override fun newArray(size: Int): Array<TrxDetailModel?> {
            return arrayOfNulls(size)
        }
    }
}
