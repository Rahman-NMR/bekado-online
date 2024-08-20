package com.bekado.bekadoonline.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityRegisterBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (auth.currentUser != null) signOut()
            else finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        onBackPressedDispatcher.addCallback(this@RegisterActivity, onBackInvokedCallback)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser

        with(binding) {
            namaDaftar.addTextChangedListener(daftarTextWatcher)
            nohpDaftar.addTextChangedListener(daftarTextWatcher)
            emailDaftar.addTextChangedListener(daftarTextWatcher)
            passwordDaftar.addTextChangedListener(daftarTextWatcher)
            konfirmasiPasswordDaftar.addTextChangedListener(daftarTextWatcher)

            updateUI(currentUser)
            actionUI(currentUser)
        }
    }

    private fun ActivityRegisterBinding.actionUI(currentUser: FirebaseUser?) {
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
                    } else registerAuth(email, password, currentUser)
                } else {
                    val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                    snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                }
            }
        }
        googleAutoLogin.setOnClickListener { if (currentUser == null) if (HelperConnection.isConnected(this@RegisterActivity)) signIn() }
        btnCancel.setOnClickListener { if (currentUser != null) signOut() }
    }

    private fun ActivityRegisterBinding.updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            if (currentUser.displayName != null) namaDaftar.setText(currentUser.displayName)
            if (currentUser.email != null) emailDaftar.setText(currentUser.email)
        }

        emailDaftar.isEnabled = currentUser == null
        outlineEmailDaftar.isEnabled = currentUser == null
        btnCancel.visibility = if (currentUser != null) View.VISIBLE else View.GONE
        googleAutoLogin.visibility = if (currentUser != null) View.GONE else View.VISIBLE
        lineGuide.visibility = if (currentUser != null) View.GONE else View.VISIBLE
    }

    private fun registerAuth(email: String, password: String, currentUser: FirebaseUser?) {
        val regist = currentUser?.updatePassword(binding.passwordDaftar.text.toString()) ?: auth.createUserWithEmailAndPassword(email, password)

        regist.addOnCompleteListener(this) {
            if (it.isSuccessful) {
                registerAkunRtdb(currentUser)
                signInSuccess()
            } else Toast.makeText(this, getString(R.string.gagal_daftar_akun), Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerAkunRtdb(currentUser: FirebaseUser?) {
        val email = binding.emailDaftar.text.toString().trim()
        val nama = binding.namaDaftar.text.toString().trim()
        val noHp = binding.nohpDaftar.text.toString().trim()

        if (currentUser != null) HelperAuth.registerAkun(currentUser.uid, db, email, nama, noHp)
    }

    private fun signIn() {
        val credentialManager = CredentialManager.create(this)

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = HelperAuth.signInByGoogle(this@RegisterActivity),
                    context = this@RegisterActivity
                )
                handleSignIn(result)
            } catch (_: GetCredentialException) {
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        loginAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (_: GoogleIdTokenParsingException) {
                    }
                }
            }
        }
    }

    private fun loginAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) signInSuccess()
        }
    }

    private fun signInSuccess() {
        val resultIntent = Intent().apply {
            putExtra(VariableConstant.ACTION_SIGN_IN_RESULT, VariableConstant.ACTION_REFRESH_UI)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun signOut() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@RegisterActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            val resultIntent = Intent().apply {
                putExtra(VariableConstant.ACTION_SIGN_IN_RESULT, VariableConstant.ACTION_SIGN_OUT)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
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
}