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
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.ui.MainActivity
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

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

            authViewModel.loginAuthWithGoogle(data) { isSuccessful ->
                if (isSuccessful) signInSuccess()
                else {
                    showToast(getString(R.string.gagal_login_google), this)
                    userViewModel.clearAkunData()
                    loadingAuthUI(false)
                }
            }
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
            val email = emailDaftar.text.toString().trim()
            val password = passwordDaftar.text.toString()
            val password2 = konfirmasiPasswordDaftar.text.toString()
            val nama = namaDaftar.text.toString().trim()
            val noHp = nohpDaftar.text.toString().trim()

            if (HelperConnection.isConnected(this@RegisterActivity)) {
                val outlineHelper =
                    listOf(outlineNamaDaftar, outlineNohpDaftar, outlineEmailDaftar, outlinePasswordDaftar, outlineKonfirmasiPasswordDaftar)

                if (outlineHelper.all { it.helperText == null }) inputValidation(nama, noHp, email, password, password2)
                else snackbarActionClose(root, getString(R.string.pastikan_no_error))
            }
        }
        googleAutoLogin.setOnClickListener {
            if (HelperConnection.isConnected(this@RegisterActivity)) signInClient.launch(authViewModel.launchSignInClient())
        }
    }

    private fun ActivityRegisterBinding.inputValidation(nama: String, noHp: String, email: String, password: String, password2: String) {
        when {
            nama.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.nama)))
            noHp.isNotEmpty() && noHp.length < 9 -> snackbarActionClose(root, getString(R.string.min_9_angka))
            email.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.email)))
            password.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.password)))
            password2.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.konfirmasi_password)))
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> snackbarActionClose(root, getString(R.string.email_invalid))
            password.length < 8 -> snackbarActionClose(root, getString(R.string.min_8_char))
            password != password2 -> snackbarActionClose(root, getString(R.string.password_berbeda))
            else -> registerAuth(email, password, nama, noHp)
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

            when (s) {
                noHpInput -> {
                    binding.outlineNohpDaftar.helperText = when {
                        noHpInput.isNullOrEmpty() -> null
                        noHpInput.length < 9 -> getString(R.string.min_9_angka)
                        else -> null
                    }
                }

                emailInput -> {
                    binding.outlineEmailDaftar.helperText = when {
                        emailInput.isNullOrEmpty() -> null
                        !Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() -> getString(R.string.email_invalid)
                        else -> null
                    }
                }

                passwordInput -> {
                    binding.outlinePasswordDaftar.helperText = when {
                        passwordInput.isNullOrEmpty() -> null
                        passwordInput.length < 8 -> getString(R.string.min_8_char)
                        else -> null
                    }
                }

                konfirmPasswordInput -> {
                    binding.outlineKonfirmasiPasswordDaftar.helperText = when {
                        konfirmPasswordInput.isNullOrEmpty() -> null
                        konfirmPasswordInput.toString() != passwordInput.toString() -> getString(R.string.password_berbeda)
                        else -> null
                    }
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
        if (isAuthenticating) signOut()
    }
}