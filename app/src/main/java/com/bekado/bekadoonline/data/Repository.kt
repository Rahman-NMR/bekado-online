package com.bekado.bekadoonline.data

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Repository(private val auth: FirebaseAuth, private val db: FirebaseDatabase, private val googleSignInClient: GoogleSignInClient) {
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun akunRef(): DatabaseReference {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            return db.getReference("akun/$uid")
        } else return db.getReference("akun/")
    }

    companion object {
        fun getInstance(
            firebaseAuth: FirebaseAuth,
            firebaseDatabase: FirebaseDatabase,
            googleSignInClient: GoogleSignInClient
        ) = Repository(firebaseAuth, firebaseDatabase, googleSignInClient)
    }
}