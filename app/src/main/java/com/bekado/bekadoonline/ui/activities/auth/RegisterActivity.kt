package com.bekado.bekadoonline.ui.activities.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.databinding.ActivityRegisterBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.ui.activities.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private var isAuthenticating = false
    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!isAuthenticating) {
                if (auth.currentUser != null) signOut()
                else finish()
            }
        }
    }
    private val signInClient = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadingAuthUI(true)
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful)
                try {
                    val account = task.getResult(ApiException::class.java)
                    loginAuthWithGoogle(account.idToken)
                } catch (_: ApiException) {
                    loadingAuthUI(false)
                }
            else loadingAuthUI(false)
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
//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))

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
        googleAutoLogin.setOnClickListener {
            if (currentUser == null)
                if (HelperConnection.isConnected(this@RegisterActivity))
                    signInClient.launch(googleSignInClient.signInIntent)
        }
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
        loadingAuthUI(true)
        val regist = currentUser?.updatePassword(binding.passwordDaftar.text.toString()) ?: auth.createUserWithEmailAndPassword(email, password)

        regist.addOnCompleteListener(this) {
            if (it.isSuccessful) {
                registerAkunRtdb(currentUser)
                signInSuccess()
            } else {
                showToast(getString(R.string.gagal_daftar_akun), this@RegisterActivity)
                loadingAuthUI(false)
            }
        }
    }

    private fun registerAkunRtdb(currentUser: FirebaseUser?) {
        val email = binding.emailDaftar.text.toString().trim()
        val nama = binding.namaDaftar.text.toString().trim()
        val noHp = binding.nohpDaftar.text.toString().trim()

        if (currentUser != null) HelperAuth.registerAkun(currentUser.uid, db, email, nama, noHp)
    }

    private fun loginAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) signInSuccess()
        }
    }

    private fun signInSuccess() {
//        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

        akunViewModel.isLoading.observe(this) { isLoading ->
            loadingAuthUI(false)
            if (!isLoading) {
                if (akunViewModel.akunModel.value != null) {
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun loadingAuthUI(isAuthLoading: Boolean) {
        isAuthenticating = isAuthLoading
        binding.progressbarRegister.isVisible = isAuthLoading
    }

    private fun signOut() {
        googleSignInClient.signOut()
        auth.signOut()
        finish()
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