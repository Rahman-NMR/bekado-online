package com.bekado.bekadoonline.domain.repositories

import android.net.Uri

interface UserUpdateRepository {
    fun updateDataAkun(pathDb: String, value: String, response: (Boolean) -> Unit)
    fun updateImageUri(imageUri: Uri, response: (Boolean) -> Unit)
}