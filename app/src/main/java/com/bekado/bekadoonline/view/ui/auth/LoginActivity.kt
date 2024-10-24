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
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.databinding.ActivityLoginBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.view.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private var isAuthenticating = false
    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!isAuthenticating) finish()
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        onBackPressedDispatcher.addCallback(this@LoginActivity, onBackInvokedCallback)

        db = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))

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
            if (HelperConnection.isConnected(this@LoginActivity)) signInClient.launch(googleSignInClient.signInIntent)
        }
    }

    private fun loginAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) signInSuccess()
        }
    }

    private fun loginAuthManual(email: String, password: String) {
        loadingAuthUI(true)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) signInSuccess()
            else {
                showToast(getString(R.string.email_pass_salah), this)
                loadingAuthUI(false)
            }
        }
    }

    private fun signInSuccess() {
//        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

        akunViewModel.isLoading.observe(this) { isLoading ->
            loadingAuthUI(false)
            if (!isLoading) {
                if (akunViewModel.akunModel.value != null) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
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
        binding.progressbarLogin.isVisible = isAuthLoading
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
}