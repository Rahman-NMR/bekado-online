package com.bekado.bekadoonline.model

import android.os.Parcel
import android.os.Parcelable

data class DetailTransaksiModel(
    var alamatModel: AlamatModel? = AlamatModel(),
    val idTransaksi: String? = "",
    val noPesanan: String? = "",
    val timestamp: String? = "",
    val statusPesanan: String? = "",
    val currency: String? = "",
    val totalItem: Long? = 0,
    val totalHarga: Long? = 0,
    val ongkir: Long? = 0,
    val metodePembayaran: String? = "",
    val totalBelanja: Long? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        null,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idTransaksi)
        parcel.writeString(noPesanan)
        parcel.writeString(timestamp)
        parcel.writeString(statusPesanan)
        parcel.writeString(currency)
        parcel.writeValue(totalItem)
        parcel.writeValue(totalHarga)
        parcel.writeValue(ongkir)
        parcel.writeString(metodePembayaran)
        parcel.writeValue(totalBelanja)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetailTransaksiModel> {
        override fun createFromParcel(parcel: Parcel): DetailTransaksiModel {
            return DetailTransaksiModel(parcel)
        }

        override fun newArray(size: Int): Array<DetailTransaksiModel?> {
            return arrayOfNulls(size)
        }
    }
}