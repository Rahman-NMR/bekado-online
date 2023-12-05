package com.bekado.bekadoonline

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.databinding.ActivityRegisterGoogleBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterGoogleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterGoogleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))

        val currentUser = auth.currentUser
        if (currentUser!!.displayName != null) binding.namaDaftar.setText(currentUser.displayName)
        if (currentUser.email != null) binding.emailDaftar.setText(currentUser.email)

        with(binding) {
            namaDaftar.addTextChangedListener(daftarTextWatcher)
            nohpDaftar.addTextChangedListener(daftarTextWatcher)
            emailDaftar.addTextChangedListener(daftarTextWatcher)
            passwordDaftar.addTextChangedListener(daftarTextWatcher)
            konfirmasiPasswordDaftar.addTextChangedListener(daftarTextWatcher)

            btnDaftar.setOnClickListener {
                if (HelperConnection.isConnected(this@RegisterGoogleActivity)) {
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
                        } else registerAuthManual(currentUser)
                    } else {
                        val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
                }
            }
            btnCancel.setOnClickListener {
                googleSignInClient.signOut()
                auth.signOut()
                finish()
            }
        }
    }

    private fun registerAuthManual(currentUser: FirebaseUser) {
        currentUser.updatePassword(binding.passwordDaftar.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                registerAkunRtdb()
                val flag = Intent(this@RegisterGoogleActivity, MainActivity::class.java)
                flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(flag)
                finish()
            }
        }
    }

    private fun registerAkunRtdb() {
        val currentUser = auth.currentUser
        val userRef = db.getReference("akun/${currentUser!!.uid}")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    //tambah data user jika ada di firebase auth tapi gada di rtdb
                    val email = binding.emailDaftar.text.toString().trim()
                    val nama = binding.namaDaftar.text.toString().trim()
                    val noHp = binding.nohpDaftar.text.toString().trim()

                    HelperAuth.registerAkun(currentUser.uid, db, email, nama, noHp)
                } else Toast.makeText(this@RegisterGoogleActivity, getString(R.string.gagal_daftar_akun), Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private val daftarTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val namaInput = binding.namaDaftar.text.toString().trim { it <= ' ' }
            val passwordInput = binding.passwordDaftar.text.toString().trim { it <= ' ' }
            val konfirmPasswordInput = binding.konfirmasiPasswordDaftar.text.toString().trim { it <= ' ' }
            binding.btnDaftar.isEnabled = namaInput.isNotEmpty() && passwordInput.isNotEmpty() && konfirmPasswordInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val noHpInput = binding.nohpDaftar.text
            val passwordInput = binding.passwordDaftar.text
            val konfirmPasswordInput = binding.konfirmasiPasswordDaftar.text

            if (s == noHpInput) {
                if (noHpInput.isNullOrEmpty()) binding.outlineNohpDaftar.helperText = null
                else {
                    if (noHpInput.length < 9) binding.outlineNohpDaftar.helperText = getString(R.string.min_9_angka)
                    else binding.outlineNohpDaftar.helperText = null
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

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
        googleSignInClient.signOut()
    }
}