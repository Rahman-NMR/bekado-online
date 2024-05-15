package com.bekado.bekadoonline.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityLoginBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
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

    private lateinit var signInClient: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, HelperAuth.clientGoogle(this))
        signInClient = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                if (task.isSuccessful)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        loginAuthWithGoogle(account.idToken)
                    } catch (_: ApiException) {
                    }
            }
        }

        with(binding) {
            emailLogin.addTextChangedListener(loginTextWatcher)
            passwordLogin.addTextChangedListener(loginTextWatcher)

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
    }

    private fun loginAuthManual(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) signInSuccess()
            else Toast.makeText(this, getString(R.string.email_pass_salah), Toast.LENGTH_SHORT).show()
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
}