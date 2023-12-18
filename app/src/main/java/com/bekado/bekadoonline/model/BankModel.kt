package com.bekado.bekadoonline.model

data class BankModel(
    val bankCode: String,
    val bankName: String,
    val swiftCode: String,
    val name: String,
    val code: String,
    val logoWiki: String,
    val noRek: String,
    var isActive: Boolean
)