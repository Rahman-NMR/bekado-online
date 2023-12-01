package com.bekado.bekadoonline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bekado.bekadoonline.databinding.ActivityLoginBinding
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 78235
        const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleAutoLogin.setOnClickListener {
            if (HelperConnection.isConnected(this)) startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception

            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Log.d(TAG, "Google Sign In Failed:", e)
                }
            } else Log.d(TAG, exception.toString())
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Log.d(TAG, "SignInBerhasil :: SUCCESS")

//                val currentUser = auth.currentUser
//                val uidAkun = currentUser?.uid.toString()
//                val userRef = db.getReference("akun").child(uidAkun)
//                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//                            finish()
//                        } else {
//                            startActivity(Intent(this@LoginActivity, RegisterGoogleActivity::class.java))
//                            finish()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {}
//                })
                finish()
                Toast.makeText(this, "Berhasil Login", Toast.LENGTH_SHORT).show()
            } else Log.w(TAG, "SignInWithCredential :: Failure", it.exception)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) finish()
    }
}