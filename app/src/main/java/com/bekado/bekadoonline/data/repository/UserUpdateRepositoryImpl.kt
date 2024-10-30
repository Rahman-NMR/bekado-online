package com.bekado.bekadoonline.data.repository

import android.net.Uri
import com.bekado.bekadoonline.domain.repositories.UserUpdateRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UserUpdateRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val storage: FirebaseStorage
) : UserUpdateRepository {
    override fun updateDataAkun(pathDb: String, value: String, response: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.getReference("akun/$uid/$pathDb").setValue(value)
                .addOnSuccessListener { response.invoke(true) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun updateImageUri(imageUri: Uri, response: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val akunRef = db.getReference("akun/$uid")
            val storageReference = storage.getReference("akun/$uid/$uid.png")

            storageReference.putFile(imageUri).addOnSuccessListener {
                storageReference.downloadUrl.addOnCompleteListener { task ->
                    val imgLink = task.result.toString()
                    akunRef.child("fotoProfil").setValue(imgLink)
                        .addOnCompleteListener { response.invoke(it.isSuccessful) }
                        .addOnFailureListener { response.invoke(false) }
                }.addOnFailureListener { response.invoke(false) }
            }.addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }
}