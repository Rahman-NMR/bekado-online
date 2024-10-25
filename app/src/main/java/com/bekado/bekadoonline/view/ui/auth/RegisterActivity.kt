package com.bekado.bekadoonline.view.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityRegisterBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.ui.MainActivity
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val authViewModel: AuthViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    private var isAuthenticating = false
    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!isAuthenticating) signOut()
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

        with(binding) {
            namaDaftar.addTextChangedListener(daftarTextWatcher)
            nohpDaftar.addTextChangedListener(daftarTextWatcher)
            emailDaftar.addTextChangedListener(daftarTextWatcher)
            passwordDaftar.addTextChangedListener(daftarTextWatcher)
            konfirmasiPasswordDaftar.addTextChangedListener(daftarTextWatcher)

            actionUI()
        }
    }

    private fun ActivityRegisterBinding.actionUI() {
        btnRegister.setOnClickListener {
            val email = binding.emailDaftar.text.toString().trim()
            val password = binding.passwordDaftar.text.toString()
            val nama = binding.namaDaftar.text.toString().trim()
            val noHp = binding.nohpDaftar.text.toString().trim()

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
                    } else registerAuth(email, password, nama, noHp)
                } else {
                    val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                    snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                }
            }
        }
        googleAutoLogin.setOnClickListener {
            if (HelperConnection.isConnected(this@RegisterActivity)) signInClient.launch(authViewModel.launchSignInClient())
        }
    }

    private fun registerAuth(email: String, password: String, nama: String, noHp: String) {
        loadingAuthUI(true)
        authViewModel.registerAuth(email, password, nama, noHp) { isSuccessful ->
            if (isSuccessful) signInSuccess()
            else {
                showToast(getString(R.string.gagal_daftar_akun), this@RegisterActivity)
                loadingAuthUI(false)
            }
        }
    }

    private fun loginAuthWithGoogle(idToken: String?) {
        authViewModel.loginAuthWithGoogle(idToken) { isSuccessful ->
            if (isSuccessful) signInSuccess()
        }
    }

    private fun signInSuccess() {
        userViewModel.isLoading().observe(this) { isLoading ->
            if (!isLoading) {
                if (userViewModel.getDataAkun().value != null) {
                    loadingAuthUI(false)
                    restartApp()
                } else {
                    authViewModel.autoRegisterToRtdb { isSuccessful ->
                        if (isSuccessful) restartApp()
                        else {
                            showToast(getString(R.string.gagal_daftar_akun), this@RegisterActivity)
                            loadingAuthUI(false)
                        }
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun loadingAuthUI(isAuthLoading: Boolean) {
        isAuthenticating = isAuthLoading
        with(binding) {
            progressbarRegister.isVisible = isAuthLoading
            googleAutoLogin.isEnabled = !isAuthLoading

            emailDaftar.isEnabled = !isAuthLoading
            outlineEmailDaftar.isEnabled = !isAuthLoading
            namaDaftar.isEnabled = !isAuthLoading
            outlineNamaDaftar.isEnabled = !isAuthLoading
            nohpDaftar.isEnabled = !isAuthLoading
            outlineNohpDaftar.isEnabled = !isAuthLoading
            passwordDaftar.isEnabled = !isAuthLoading
            outlinePasswordDaftar.isEnabled = !isAuthLoading
            konfirmasiPasswordDaftar.isEnabled = !isAuthLoading
            outlineKonfirmasiPasswordDaftar.isEnabled = !isAuthLoading
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

    private fun signOut() {
        userViewModel.clearAkunData()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        signOut()
    }
}