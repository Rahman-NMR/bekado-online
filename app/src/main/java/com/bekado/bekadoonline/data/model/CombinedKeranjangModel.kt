package com.bekado.bekadoonline.data.model

import android.os.Parcel
import android.os.Parcelable

data class CombinedKeranjangModel(
    var produkModel: ProdukModel?,
    var keranjangModel: KeranjangModel?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ProdukModel::class.java.classLoader),
        parcel.readParcelable(KeranjangModel::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(produkModel, flags)
        parcel.writeParcelable(keranjangModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CombinedKeranjangModel> {
        override fun createFromParcel(parcel: Parcel): CombinedKeranjangModel {
            return CombinedKeranjangModel(parcel)
        }

        override fun newArray(size: Int): Array<CombinedKeranjangModel?> {
            return arrayOfNulls(size)
        }
    }
}