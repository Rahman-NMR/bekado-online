package com.bekado.bekadoonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.bekado.bekadoonline.databinding.ActivityUbahPasswordBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class UbahPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUbahPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        with(binding) {
            passwordSekarang.addTextChangedListener(pwTextWatcher)
            passwordBaru.addTextChangedListener(pwTextWatcher)
            konfirmasiPassword.addTextChangedListener(pwTextWatcher)

            appBar.setNavigationOnClickListener { finish() }

            btnKonfirmasi.setOnClickListener {
                if (HelperConnection.isConnected(this@UbahPasswordActivity)) {
                    if (outlinePasswordSekarang.helperText == null &&
                        outlinePasswordBaru.helperText == null &&
                        outlineKonfirmasiPassword.helperText == null
                    ) {
                        val passwordInput = passwordBaru.text
                        val konfirmPasswordInput = konfirmasiPassword.text

                        if (konfirmPasswordInput.toString() != passwordInput.toString()) {
                            val snackbar = Snackbar.make(root, getString(R.string.password_berbeda), Snackbar.LENGTH_LONG)
                            snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                        } else ubahPassword()
                    } else {
                        val snackbar = Snackbar.make(root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
                }
            }
        }
    }

    private fun ubahPassword() {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, binding.passwordSekarang.text.toString())

            user.reauthenticate(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    showToast("${getString(R.string.password)} ${getString(R.string.berhasil_mengubah)}", this)
                    user.updatePassword(binding.passwordBaru.text.toString())
                        .addOnCompleteListener { task -> if (task.isSuccessful) finish() }
                } else showToast(getString(R.string.pass_salah), this)
            }
        }
    }

    private val pwTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val passwordSkrngInput = binding.passwordSekarang.text.toString().trim { it <= ' ' }
            val passwordBaruInput = binding.passwordBaru.text.toString().trim { it <= ' ' }
            val konfirmPasswordInput = binding.konfirmasiPassword.text.toString().trim { it <= ' ' }
            binding.btnKonfirmasi.isEnabled =
                passwordSkrngInput.isNotEmpty() && passwordBaruInput.isNotEmpty() && konfirmPasswordInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val passwordSkrngInput = binding.passwordSekarang.text
            val passwordBaruInput = binding.passwordBaru.text
            val konfirmPasswordInput = binding.konfirmasiPassword.text

            if (s == passwordSkrngInput) {
                if (passwordSkrngInput.isNullOrEmpty()) binding.outlinePasswordSekarang.helperText = null
                else {
                    if (passwordSkrngInput.toString().length < 8) binding.outlinePasswordSekarang.helperText = getString(R.string.min_8_char)
                    else binding.outlinePasswordSekarang.helperText = null
                }
            } else if (s == passwordBaruInput) {
                if (passwordBaruInput.isNullOrEmpty()) binding.outlinePasswordBaru.helperText = null
                else {
                    if (passwordBaruInput.toString().length < 8) binding.outlinePasswordBaru.helperText = getString(R.string.min_8_char)
                    else binding.outlinePasswordBaru.helperText = null
                }
            } else if (s == konfirmPasswordInput) {
                if (konfirmPasswordInput.isNullOrEmpty()) binding.outlineKonfirmasiPassword.helperText = null
                else {
                    if (konfirmPasswordInput.toString() != passwordBaruInput.toString())
                        binding.outlineKonfirmasiPassword.helperText = getString(R.string.password_berbeda)
                    else binding.outlineKonfirmasiPassword.helperText = null
                }
            }
        }
    }
}