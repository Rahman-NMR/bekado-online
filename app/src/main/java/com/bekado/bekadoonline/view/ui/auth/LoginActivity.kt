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
import com.bekado.bekadoonline.databinding.ActivityLoginBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.ui.MainActivity
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        onBackPressedDispatcher.addCallback(this@LoginActivity, onBackInvokedCallback)

        with(binding) {
            emailLogin.addTextChangedListener(loginTextWatcher)
            passwordLogin.addTextChangedListener(loginTextWatcher)

            actionUI()
        }
    }

    private fun ActivityLoginBinding.actionUI() {
        btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString().trim()
            val password = binding.passwordLogin.text.toString()

            if (HelperConnection.isConnected(this@LoginActivity)) {
                if (outlineEmailLogin.helperText == null && outlinePasswordLogin.helperText == null) inputValidation(email, password)
                else snackbarActionClose(root, getString(R.string.pastikan_no_error))
            }
        }
        btnLupaPassword.setOnClickListener { startActivity(Intent(this@LoginActivity, LupaPasswordActivity::class.java)) }
        googleAutoLogin.setOnClickListener {
            if (HelperConnection.isConnected(this@LoginActivity)) signInClient.launch(authViewModel.launchSignInClient())
        }
    }

    private fun ActivityLoginBinding.inputValidation(email: String, password: String) {
        when {
            email.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.email)))
            password.isEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.password)))
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> snackbarActionClose(root, getString(R.string.email_invalid))
            passwordLogin.length() < 8 -> snackbarActionClose(root, getString(R.string.min_8_char))
            else -> loginAuthManual(email, password)
        }
    }

    private fun loginAuthManual(email: String, password: String) {
        loadingAuthUI(true)
        authViewModel.loginAuthManual(email, password) { isSuccessful ->
            if (isSuccessful) signInSuccess()
            else {
                showToast(getString(R.string.email_pass_salah), this)
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
                            showToast(getString(R.string.email_pass_salah), this)
                            loadingAuthUI(false)
                        }
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun loadingAuthUI(isAuthLoading: Boolean) {
        isAuthenticating = isAuthLoading
        with(binding) {
            progressbarLogin.isVisible = isAuthLoading
            googleAutoLogin.isEnabled = !isAuthLoading
            btnLupaPassword.isEnabled = !isAuthLoading

            emailLogin.isEnabled = !isAuthLoading
            outlineEmailLogin.isEnabled = !isAuthLoading
            passwordLogin.isEnabled = !isAuthLoading
            outlinePasswordLogin.isEnabled = !isAuthLoading
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
            when (s) {
                binding.emailLogin.text -> {
                    binding.outlineEmailLogin.helperText = when {
                        binding.emailLogin.text.isNullOrEmpty() -> null
                        !Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() -> getString(R.string.email_invalid)
                        else -> null
                    }
                }

                binding.passwordLogin.text -> {
                    binding.outlinePasswordLogin.helperText = when {
                        binding.passwordLogin.text.isNullOrEmpty() -> null
                        binding.passwordLogin.length() < 8 -> getString(R.string.min_8_char)
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