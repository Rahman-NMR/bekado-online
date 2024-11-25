package com.bekado.bekadoonline.view.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityUbahPasswordBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

class UbahPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUbahPasswordBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val authViewModel: AuthViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        dataAkunHandler()

        with(binding) {
            passwordSekarang.addTextChangedListener(pwTextWatcher)
            passwordBaru.addTextChangedListener(pwTextWatcher)
            konfirmasiPassword.addTextChangedListener(pwTextWatcher)

            appBar.setNavigationOnClickListener { finish() }
            btnKonfirmasi.setOnClickListener {
                val password0 = passwordSekarang.text
                val password1 = passwordBaru.text
                val password2 = konfirmasiPassword.text

                if (HelperConnection.isConnected(this@UbahPasswordActivity)) {
                    val outlineHelper = listOf(outlinePasswordSekarang, outlinePasswordBaru, outlineKonfirmasiPassword)

                    if (outlineHelper.any { it.helperText == null }) inputValidation(password0, password1, password2)
                    else snackbarActionClose(root, getString(R.string.pastikan_no_error))
                }
            }
        }
    }

    private fun ActivityUbahPasswordBinding.inputValidation(password0: Editable?, password1: Editable?, password2: Editable?) {
        when {
            password0.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.password)))
            password1.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.password)))
            password2.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.konfirmasi_password)))
            password0.length < 8 -> snackbarActionClose(root, getString(R.string.min_8_char))
            password1.length < 8 -> snackbarActionClose(root, getString(R.string.min_8_char))
            password1.toString() != password2.toString() -> snackbarActionClose(root, getString(R.string.password_berbeda))
            else -> ubahPassword()
        }
    }

    private fun ubahPassword() {
        authViewModel.reAuthenticate(binding.passwordSekarang.text.toString(), { inputNotEmpty ->
            if (!inputNotEmpty) showToast(getString(R.string.tidak_dapat_kosong, getString(R.string.password)), this)
        }, { dataExist, isSuccessful ->
            when {
                dataExist && isSuccessful -> {
                    authViewModel.updatePassword(binding.passwordBaru.text.toString(), { inputNotEmpty ->
                        if (!inputNotEmpty) showToast(getString(R.string.tidak_dapat_kosong, getString(R.string.password_baru)), this)
                    }, { isUpdated ->
                        if (isUpdated) {
                            showToast(getString(R.string.berhasil_diperbarui, getString(R.string.password)), this)
                            finish()
                        } else showToast(getString(R.string.gagal_memperbarui_x, getString(R.string.password)), this)
                    })
                }

                !dataExist && !isSuccessful -> showToast(getString(R.string.masalah_database), this)
                else -> showToast(getString(R.string.pass_salah), this)
            }
        })

    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            if (akun == null) finish()
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

            when (s) {
                passwordSkrngInput -> {
                    binding.outlinePasswordSekarang.helperText = when {
                        passwordSkrngInput.isNullOrEmpty() -> null
                        passwordSkrngInput.toString().length < 8 -> getString(R.string.min_8_char)
                        else -> null
                    }
                }

                passwordBaruInput -> {
                    binding.outlinePasswordBaru.helperText = when {
                        passwordBaruInput.isNullOrEmpty() -> null
                        passwordBaruInput.toString().length < 8 -> getString(R.string.min_8_char)
                        else -> null
                    }
                }

                konfirmPasswordInput -> {
                    binding.outlineKonfirmasiPassword.helperText = when {
                        konfirmPasswordInput.isNullOrEmpty() -> null
                        konfirmPasswordInput.toString() != passwordBaruInput.toString() -> getString(R.string.password_berbeda)
                        else -> null
                    }
                }
            }
        }
    }
}