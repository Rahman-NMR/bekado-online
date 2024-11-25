package com.bekado.bekadoonline.view.ui.bottomsheet

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.BottomsheetHubungkanAkunBinding
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetHubungkanAkun(
    private val context: Context,
    private val userViewModel: UserViewModel,
) {
    private var bindingBS: BottomsheetHubungkanAkunBinding = BottomsheetHubungkanAkunBinding.inflate(LayoutInflater.from(context))
    private var dialog: BottomSheetDialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)

    init {
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(result: (String, String) -> Unit) {
        with(bindingBS) {
            emailVerif.addTextChangedListener(textWatcherVerif)
            passwordVerif.addTextChangedListener(textWatcherVerif)

            title.text = context.getString(R.string.link_account)
            hint.text = context.getString(R.string.link_account_hint)

            emailVerif.setText(userViewModel.getDataAkun().value?.email)
            emailVerif.isEnabled = false
            outlineEmailVerif.isEnabled = false

            btnVerif.setOnClickListener {
                val emailV = emailVerif.text.toString().trim()
                val passwordV = passwordVerif.text.toString()

                if (HelperConnection.isConnected(context)) {
                    when {
                        emailV.isEmpty() -> {
                            snackbarActionClose(root, context.getString(R.string.x_masih_kosong, context.getString(R.string.email)))
                        }

                        passwordV.isEmpty() -> {
                            snackbarActionClose(root, context.getString(R.string.x_masih_kosong, context.getString(R.string.password)))
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(emailV).matches() -> {
                            snackbarActionClose(root, context.getString(R.string.email_invalid))
                        }

                        passwordV.length < 8 -> {
                            snackbarActionClose(root, context.getString(R.string.min_8_char))
                        }

                        outlinePasswordVerif.helperText != null || outlineEmailVerif.helperText != null -> {
                            snackbarActionClose(root, context.getString(R.string.pastikan_no_error))
                        }

                        else -> {
                            result.invoke(emailV, passwordV)
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        dialog.show()
        dialog.setOnDismissListener {
            bindingBS.emailVerif.removeTextChangedListener(textWatcherVerif)
            bindingBS.passwordVerif.removeTextChangedListener(textWatcherVerif)
        }
    }

    private val textWatcherVerif: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val emailInput = bindingBS.emailVerif.text.toString().trim { it <= ' ' }
            val passwordInput = bindingBS.passwordVerif.text.toString().trim { it <= ' ' }
            bindingBS.btnVerif.isEnabled = emailInput.isNotEmpty() && passwordInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            when (s) {
                bindingBS.emailVerif.text -> {
                    bindingBS.outlineEmailVerif.helperText = when {
                        bindingBS.emailVerif.text.isNullOrEmpty() -> null
                        !Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() -> context.getString(R.string.email_invalid)
                        else -> null
                    }
                }

                bindingBS.passwordVerif.text -> {
                    bindingBS.outlinePasswordVerif.helperText = when {
                        bindingBS.passwordVerif.text.isNullOrEmpty() -> null
                        bindingBS.passwordVerif.length() < 8 -> context.getString(R.string.min_8_char)
                        else -> null
                    }
                }
            }
        }
    }
}