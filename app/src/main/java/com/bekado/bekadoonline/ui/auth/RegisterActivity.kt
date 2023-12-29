package com.bekado.bekadoonline.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.ui.MainActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityRegisterBinding
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 45697
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))

        with(binding) {
            namaDaftar.addTextChangedListener(daftarTextWatcher)
            nohpDaftar.addTextChangedListener(daftarTextWatcher)
            emailDaftar.addTextChangedListener(daftarTextWatcher)
            passwordDaftar.addTextChangedListener(daftarTextWatcher)
            konfirmasiPasswordDaftar.addTextChangedListener(daftarTextWatcher)

            btnRegister.setOnClickListener {
                val email = binding.emailDaftar.text.toString().trim()
                val password = binding.passwordDaftar.text.toString()

                if (HelperConnection.isConnected(this@RegisterActivity)) {
                    if (binding.outlineNamaDaftar.helperText == null
                        && binding.outlineNohpDaftar.helperText == null
                        && binding.outlineEmailDaftar.helperText == null
                        && binding.outlinePasswordDaftar.helperText == null
                        && binding.outlineKonfirmasiPasswordDaftar.helperText == null
                    ) {
                        val passwordInput = binding.passwordDaftar.text
                        val konfirmPasswordInput = binding.konfirmasiPasswordDaftar.text

                        if (konfirmPasswordInput.toString() != passwordInput.toString()) {
                            val snackbar = Snackbar.make(binding.root, getString(R.string.password_berbeda), Snackbar.LENGTH_LONG)
                            snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                        } else registerAuthManual(email, password)
                    } else {
                        val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
                }
            }
            googleAutoLogin.setOnClickListener {
                if (HelperConnection.isConnected(this@RegisterActivity)) startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun registerAuthManual(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                registerAkunRtdb()
                val flag = Intent(this@RegisterActivity, MainActivity::class.java)
                flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(flag)
                finish()
            } else Toast.makeText(this, getString(R.string.gagal_daftar_akun), Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerAkunRtdb() {
        val currentUser = auth.currentUser
        val email = binding.emailDaftar.text.toString().trim()
        val nama = binding.namaDaftar.text.toString().trim()
        val noHp = binding.nohpDaftar.text.toString().trim()

        if (currentUser != null) HelperAuth.registerAkun(currentUser.uid, db, email, nama, noHp)
    }

    private val daftarTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val namaInput = binding.namaDaftar.text.toString().trim { it <= ' ' }
            val emailInput = binding.emailDaftar.text.toString().trim { it <= ' ' }
            val passwordInput = binding.passwordDaftar.text.toString().trim { it <= ' ' }
            val konfirmPasswordInput = binding.konfirmasiPasswordDaftar.text.toString().trim { it <= ' ' }
            binding.btnRegister.isEnabled =
                namaInput.isNotEmpty() && emailInput.isNotEmpty() && passwordInput.isNotEmpty() && konfirmPasswordInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val noHpInput = binding.nohpDaftar.text
            val emailInput = binding.emailDaftar.text
            val passwordInput = binding.passwordDaftar.text
            val konfirmPasswordInput = binding.konfirmasiPasswordDaftar.text

            if (s == noHpInput) {
                if (noHpInput.isNullOrEmpty()) binding.outlineNohpDaftar.helperText = null
                else {
                    if (noHpInput.length < 9) binding.outlineNohpDaftar.helperText = getString(R.string.min_9_angka)
                    else binding.outlineNohpDaftar.helperText = null
                }
            } else if (s == emailInput) {
                if (emailInput.isNullOrEmpty()) binding.outlineEmailDaftar.helperText = null
                else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches())
                        binding.outlineEmailDaftar.helperText = getString(R.string.email_invalid)
                    else binding.outlineEmailDaftar.helperText = null
                }
            } else if (s == passwordInput) {
                if (passwordInput.isNullOrEmpty()) binding.outlinePasswordDaftar.helperText = null
                else {
                    if (passwordInput.toString().length < 8) binding.outlinePasswordDaftar.helperText = getString(R.string.min_8_char)
                    else binding.outlinePasswordDaftar.helperText = null
                }
            } else if (s == konfirmPasswordInput) {
                if (konfirmPasswordInput.isNullOrEmpty()) binding.outlineKonfirmasiPasswordDaftar.helperText = null
                else {
                    if (konfirmPasswordInput.toString() != passwordInput.toString())
                        binding.outlineKonfirmasiPasswordDaftar.helperText = getString(R.string.password_berbeda)
                    else binding.outlineKonfirmasiPasswordDaftar.helperText = null
                }
            }
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
                            val flag = Intent(this@RegisterActivity, MainActivity::class.java)
                            flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(flag)
                            finish()
                        } else {
                            startActivity(Intent(this@RegisterActivity, RegisterGoogleActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
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
            } else {
                Log.d(TAG, exception.toString())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) finish()
    }
}