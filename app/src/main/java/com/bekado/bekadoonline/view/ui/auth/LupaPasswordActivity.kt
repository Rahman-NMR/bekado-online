package com.bekado.bekadoonline.view.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityLupaPasswordBinding
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.firebase.auth.FirebaseAuth

class LupaPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLupaPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        with(binding) {
            emailLupapw.addTextChangedListener(forgetTextWatcher)

            btnResetPassword.setOnClickListener {
                val emailTxt = emailLupapw.text

                if (outlineEmailLupapw.helperText == null) inputValidation(emailTxt)
                else snackbarActionClose(root, getString(R.string.pastikan_no_error))
            }
        }
    }

    private fun ActivityLupaPasswordBinding.inputValidation(emailTxt: Editable?) {
        when {
            emailTxt.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.email)))
            !Patterns.EMAIL_ADDRESS.matcher(emailTxt.toString()).matches() -> snackbarActionClose(root, getString(R.string.email_invalid))
            else -> showAlrtDialog(emailTxt)
        }
    }

    private fun showAlrtDialog(emailTxt: Editable) {
        showAlertDialog(
            getString(R.string.reset_password) + "?",
            getString(R.string.kirim_tautan_ke_email, emailTxt.toString()),
            getString(R.string.reset),
            this@LupaPasswordActivity,
            getColor(R.color.blue_grey_700)
        ) { if (HelperConnection.isConnected(this)) resetPassword(emailTxt) }
    }

    private fun resetPassword(emailReset: Editable) {
        auth.sendPasswordResetEmail(emailReset.toString().trim()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToastL(getString(R.string.tautan_reset_pw_sukses), this)
                finish()
            } else showToast("${task.exception?.message}", this)
        }
    }

    private val forgetTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val emailInput = binding.emailLupapw.text.toString().trim { it <= ' ' }
            binding.btnResetPassword.isEnabled = emailInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            binding.outlineEmailLupapw.helperText = when {
                binding.emailLupapw.text.isNullOrEmpty() -> getString(R.string.tidak_dapat_kosong, getString(R.string.email))
                !Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() -> getString(R.string.email_invalid)
                else -> null
            }
        }
    }
}