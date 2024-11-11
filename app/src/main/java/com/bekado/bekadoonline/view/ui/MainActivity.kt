package com.bekado.bekadoonline.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityMainBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val authViewModel: AuthViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            if (navController.currentDestination?.id == R.id.navigation_beranda) {
                if (backPressedTime + 2000 > System.currentTimeMillis()) finish()
                else showToast(getString(R.string.press_back_again), this@MainActivity)
                backPressedTime = System.currentTimeMillis()
            } else navController.navigateUp()
        }

    }
    private var backPressedTime: Long = 0
    private var splashOpen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { splashOpen }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        onBackPressedDispatcher.addCallback(this@MainActivity, onBackInvokedCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            splashOpen = false

            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_beranda, R.id.navigation_transaksi, R.id.navigation_profil))
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            viewModelHandler()
        }, 400)
    }

    private fun viewModelHandler() {
        userViewModel.isLoading().observe(this) { isLoading ->
            if (!isLoading && userViewModel.getDataAkun().value == null) {
                authViewModel.autoRegisterToRtdb { isSuccessful ->
                    if (isSuccessful) restartApp()
                    else {
                        showToastL(getString(R.string.account_problem), this)

                        Handler(Looper.getMainLooper()).postDelayed({
                            userViewModel.clearAkunData()
                            restartApp()
                        }, 3210)
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}