package com.bekado.bekadoonline.data.model

data class BuktiPembayaranModel(
    var biayaTransfer: Long? = 0,
    var buktiTransaksi: String? = "",
    var fotoBank: String? = "",
    var namaBank: String? = "",
    var noRek: String? = "",
    var pemilikBank: String? = ""
)
