package com.bekado.bekadoonline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.bekado.bekadoonline.databinding.ActivityLoginBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
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
        db = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))

        with(binding) {
            emailLogin.addTextChangedListener(loginTextWatcher)
            passwordLogin.addTextChangedListener(loginTextWatcher)

            btnDaftarBaru.setOnClickListener { startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) }
            btnLogin.setOnClickListener {
                val email = binding.emailLogin.text.toString().trim()
                val password = binding.passwordLogin.text.toString()
                if (HelperConnection.isConnected(this@LoginActivity)) {
                    if (binding.outlineEmailLogin.helperText == null && binding.outlinePasswordLogin.helperText == null)
                        loginAuthManual(email, password)
                    else {
                        val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
                }
            }
            btnLupaPassword.setOnClickListener { startActivity(Intent(this@LoginActivity, LupaPasswordActivity::class.java)) }
            googleAutoLogin.setOnClickListener {
                if (HelperConnection.isConnected(this@LoginActivity)) startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun loginAuthManual(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val flag = Intent(this@LoginActivity, MainActivity::class.java)
                flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(flag)
                finish()
            } else Toast.makeText(this, getString(R.string.email_pass_salah), Toast.LENGTH_SHORT).show()
        }
    }

    private val loginTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val emailInput = binding.emailLogin.text.toString().trim { it <= ' ' }
            val passwordInput = binding.passwordLogin.text.toString().trim { it <= ' ' }
            binding.btnLogin.isEnabled = emailInput.isNotEmpty() && passwordInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            if (s == binding.emailLogin.text) {
                if (binding.emailLogin.text.isNullOrEmpty()) binding.outlineEmailLogin.helperText = null
                else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches())
                        binding.outlineEmailLogin.helperText = getString(R.string.email_invalid)
                    else binding.outlineEmailLogin.helperText = null
                }
            } else if (s == binding.passwordLogin.text) {
                if (binding.passwordLogin.text.isNullOrEmpty()) binding.outlinePasswordLogin.helperText = null
                else {
                    if (binding.passwordLogin.length() < 8) binding.outlinePasswordLogin.helperText = getString(R.string.min_8_char)
                    else binding.outlinePasswordLogin.helperText = null
                }
            }
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
                    loginAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Log.d(TAG, "Google Sign In Failed:", e)
                }
            } else Log.d(TAG, exception.toString())
        }
    }

    private fun loginAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val currentUser = auth.currentUser
                val uidAkun = currentUser?.uid.toString()
                val userRef = db.getReference("akun/$uidAkun")

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val flag = Intent(this@LoginActivity, MainActivity::class.java)
                            flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(flag)
                            finish()
                        } else {
                            startActivity(Intent(this@LoginActivity, RegisterGoogleActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) finish()
    }
}