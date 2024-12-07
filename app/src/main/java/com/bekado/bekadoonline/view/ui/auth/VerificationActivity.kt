package com.bekado.bekadoonline.view.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityVerificationBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.RES_DIFFRNT
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.RES_MISCAST
import com.bekado.bekadoonline.view.ui.bottomsheet.BottomSheetHubungkanAkun
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding
    private lateinit var signInClient: ActivityResultLauncher<Intent>

    private val authViewModel: AuthViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    private var isLoading = false
    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!isLoading) backPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        onBackPressedDispatcher.addCallback(this@VerificationActivity, onBackInvokedCallback)
        signInClient = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                loadingUI(true)
                resultGoogleID(result)
            }
        }

        dataAkunHandler()
        uiHandler()
        actionUI()
    }

    private fun actionUI() {
        binding.currentAccount.text = userViewModel.currentUser()?.email ?: getString(R.string.tidak_ada_data)

        binding.appBar.setNavigationOnClickListener { backPressed() }
        binding.linkEmail.setOnClickListener { actionLinkEmail() }
        binding.linkGoogle.setOnClickListener {
            if (HelperConnection.isConnected(this)) signInClient.launch(authViewModel.launchSignInClient())
        }
    }

    private fun uiHandler() {
        val googleVerified = userViewModel.isVerified().isGoogleVerified ?: false
        val emailVerified = userViewModel.isVerified().isEmailVerified ?: false

        binding.linkGoogle.isVisible = !googleVerified
        binding.linkEmail.isVisible = !emailVerified
        binding.linkGoogleStatus.text = if (googleVerified) "Terhubung" else getString(R.string.strip)
        binding.linkEmailStatus.text = if (emailVerified) "Terhubung" else getString(R.string.strip)
    }

    private fun actionLinkEmail() {
        BottomSheetHubungkanAkun(this@VerificationActivity, userViewModel).showDialog { emailVerif, passwordVerif ->
            loadingUI(true)

            val credential = EmailAuthProvider.getCredential(emailVerif, passwordVerif)
            linkCredential(credential, "Email")
        }
    }

    private fun resultGoogleID(result: ActivityResult) {
        authViewModel.linkToGoogle(result.data) { isSuccesful: Boolean, value: String ->
            when (isSuccesful) {
                true -> {
                    val credential = GoogleAuthProvider.getCredential(value, null)
                    linkCredential(credential, "Google")
                }

                false -> {
                    loadingUI(false)
                    when (value) {
                        RES_DIFFRNT -> showToastL(getString(R.string.different_email), this@VerificationActivity)
                        RES_MISCAST -> showToast(getString(R.string.miscast_google_akun), this@VerificationActivity)
                    }
                }
            }
        }
    }

    private fun linkCredential(credential: AuthCredential, toastText: String) {
        authViewModel.linkCredentials(credential) { isSuccessful ->
            if (isSuccessful) {
                showToast(getString(R.string.x_berhasil_terhubung, toastText), this@VerificationActivity)
                uiHandler()
                loadingUI(false)
            } else {
                showToast(getString(R.string.gagal_menghubungkan_x, toastText), this@VerificationActivity)
                loadingUI(false)
            }
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this@VerificationActivity) { user ->
            if (user == null) backPressed()
        }
    }

    private fun loadingUI(isLoading: Boolean) {
        this@VerificationActivity.isLoading = isLoading
        binding.root.isEnabled = isLoading
        binding.linkGoogle.isEnabled = !isLoading
        binding.linkEmail.isEnabled = !isLoading
        binding.progressbarVerif.isVisible = isLoading
    }

    private fun backPressed() = finish()
}