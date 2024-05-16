package com.bekado.bekadoonline.ui.activities.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityLupaPasswordBinding
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.google.android.material.snackbar.Snackbar
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
                if (outlineEmailLupapw.helperText == null) {
                    val emailTxt = emailLupapw.text
                    if (emailTxt.isNullOrEmpty())
                        showToast("${getString(R.string.email)} ${getString(R.string.tidak_dapat_kosong)}", this@LupaPasswordActivity)
                    else showAlrtDialog(emailTxt)
                } else {
                    val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                    snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                }
            }
        }
    }

    private fun showAlrtDialog(emailTxt: Editable) {
        showAlertDialog(
            getString(R.string.reset_password) + "?",
            getString(R.string.kirim_tautan_ke_email) + " " + emailTxt.toString(),
            getString(R.string.reset),
            this@LupaPasswordActivity,
            getColor(R.color.blue_grey_700)
        ) { if (HelperConnection.isConnected(this)) resetPassword() }
    }

    private fun resetPassword() {
        val emailReset = binding.emailLupapw.text.toString().trim()

        auth.sendPasswordResetEmail(emailReset).addOnCompleteListener { task ->
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
            if (binding.emailLupapw.text.isNullOrEmpty()) binding.outlineEmailLupapw.helperText = null
            else {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches())
                    binding.outlineEmailLupapw.helperText = getString(R.string.email_invalid)
                else binding.outlineEmailLupapw.helperText = null
            }
        }
    }
}