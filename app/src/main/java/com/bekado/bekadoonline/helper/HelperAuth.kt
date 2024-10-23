package com.bekado.bekadoonline.helper

import android.content.Context
import android.view.MenuItem
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.helper.Helper.showToast
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.FirebaseDatabase

object HelperAuth {
    fun clientGoogle(context: Context): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
    }

    fun registerAkun(uidAkun: String, db: FirebaseDatabase, email: String, nama: String, noHp: String) {
        val userRef = db.getReference("akun/$uidAkun")
        val user = HashMap<String, Any>()
        val defImgLink = "https://firebasestorage.googleapis.com/v0/b/bekado-online.appspot.com/o/img_profile.png"
        val token = "d5508c9e-43ea-4ddb-a42a-9e52de8b5c4c"

        user["email"] = email
        user["fotoProfil"] = "$defImgLink?alt=media&token=$token"
        user["nama"] = nama
        user["noHp"] = noHp
        user["statusAdmin"] = false
        user["uid"] = uidAkun
        userRef.setValue(user)
    }

    fun adminKeranjangState(context: Context, item: MenuItem) {
        when (item.itemId) {
            R.id.menu_keranjang -> showToast("Admin tidak bisa ke keranjang", context)
        }
    }
}