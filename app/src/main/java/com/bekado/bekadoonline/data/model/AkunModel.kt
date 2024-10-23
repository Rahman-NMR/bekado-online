package com.bekado.bekadoonline.data.model

import android.os.Parcel
import android.os.Parcelable

data class AkunModel(
    val email: String? = "",
    val fotoProfil: String? = "",
    val nama: String? = "",
    val noHp: String? = "",
    val statusAdmin: Boolean = false,
    val uid: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(fotoProfil)
        parcel.writeString(nama)
        parcel.writeString(noHp)
        parcel.writeByte(if (statusAdmin) 1 else 0)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AkunModel> {
        override fun createFromParcel(parcel: Parcel): AkunModel {
            return AkunModel(parcel)
        }

        override fun newArray(size: Int): Array<AkunModel?> {
            return arrayOfNulls(size)
        }
    }
}