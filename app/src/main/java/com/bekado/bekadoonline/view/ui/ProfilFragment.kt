package com.bekado.bekadoonline.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.databinding.FragmentProfilBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.view.ui.admn.KategoriListActivity
import com.bekado.bekadoonline.view.ui.auth.LoginActivity
import com.bekado.bekadoonline.view.ui.auth.RegisterActivity
import com.bekado.bekadoonline.view.ui.auth.UbahPasswordActivity
import com.bekado.bekadoonline.view.ui.profil.AboutBekadoActivity
import com.bekado.bekadoonline.view.ui.profil.AkunSayaActivity
import com.bekado.bekadoonline.view.ui.profil.AlamatActivity
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.AuthViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(requireActivity()) }
    private val authViewModel: AuthViewModel by viewModels { UserViewModelFactory.getInstance(requireActivity()) }
    private val transaksiViewModel: TransaksiViewModel by viewModels { TransaksiViewModelFactory.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataAkunHandler()

        with(binding) {
            notNullLayout.isVisible = userViewModel.currentUser() != null
            nullLayout.isGone = userViewModel.currentUser() != null

            btnLogin.setOnClickListener { startAuthLoginActivity(true) }
            btnRegister.setOnClickListener { startAuthLoginActivity(false) }
            btnInformasiToko.setOnClickListener { startActivity(Intent(context, AboutBekadoActivity::class.java)) }
            btnLogout.setOnClickListener { showAlertDialog() }
        }
    }

    private fun FragmentProfilBinding.uiVerifiedHandler(akunModel: AkunModel?, emailVerified: Boolean, googleVerified: Boolean) {
        btnVerifiedGoogle.apply {
            isVisible = akunModel != null && !googleVerified
            isEnabled = akunModel != null && !googleVerified
            setOnClickListener { showToast("Dalam pengembangan", requireActivity())/*startActivity(Intent(context, x::class.java))*/ }
        }
        btnVerifiedEmail.apply {
            isVisible = akunModel != null && !emailVerified
            isEnabled = akunModel != null && !emailVerified
            setOnClickListener { showToast("Dalam pengembangan", requireActivity())/*startActivity(Intent(context, x::class.java))*/ }
        }

        badgeGoogleVerified.apply {
            isVisible = akunModel != null && googleVerified
            setOnClickListener { showToast(getString(R.string.can_login_by_x, "akun Google"), requireActivity()) }
        }
        badgeEmailVerified.apply {
            isVisible = akunModel != null && emailVerified
            setOnClickListener { showToast(getString(R.string.can_login_by_x, "Email dan Password"), requireActivity()) }
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(viewLifecycleOwner) { akunModel ->
            dataTransaksiHandler(akunModel)

            with(binding) {
                val emailVerified = userViewModel.isVerified().isEmailVerified ?: false
                val googleVerified = userViewModel.isVerified().isGoogleVerified ?: false
                uiVerifiedHandler(akunModel, emailVerified, googleVerified)

                badgeAdmin.isVisible = akunModel != null && akunModel.statusAdmin
                btnAdminKategoriProduk.isVisible = akunModel != null && akunModel.statusAdmin

                namaProfil.isVisible = !akunModel?.nama.isNullOrEmpty()
                namaProfil.text = if (!akunModel?.nama.isNullOrEmpty()) akunModel?.nama else getString(R.string.strip)
                emailProfil.text = akunModel?.email

                val borderColorProfil = if (akunModel?.statusAdmin == true) R.color.verif else R.color.blue_700
                fotoProfil.borderColor = ContextCompat.getColor(requireContext(), borderColorProfil)

                val fotopp = if (akunModel?.fotoProfil.isNullOrEmpty()) null else akunModel?.fotoProfil
                Glide.with(requireContext()).load(fotopp)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder_profil)
                    .fallback(R.drawable.img_fallback_profil)
                    .error(R.drawable.img_error_profil)
                    .into(fotoProfil)

                btnAkunSaya.isEnabled = akunModel != null
                btnDetailAlamat.isEnabled = akunModel != null
                btnUbahPassword.isEnabled = akunModel != null && emailVerified
                btnAdminKategoriProduk.isEnabled = akunModel != null

                if (akunModel != null) {
                    btnAdminKategoriProduk.setOnClickListener { startActivity(Intent(context, KategoriListActivity::class.java)) }
                    btnAkunSaya.setOnClickListener { startActivity(Intent(context, AkunSayaActivity::class.java)) }
                    btnDetailAlamat.setOnClickListener { startActivity(Intent(context, AlamatActivity::class.java)) }
                    btnUbahPassword.setOnClickListener { startActivity(Intent(context, UbahPasswordActivity::class.java)) }
                }
            }

        }
        userViewModel.isLoading().observe(viewLifecycleOwner) { isLoading ->
            binding.akunSaya.isVisible = !isLoading
            binding.shimmerAkunSaya.isVisible = isLoading
            binding.shimmerAkunSaya.apply { if (isLoading) startShimmer() else stopShimmer() }

            if (!isLoading) {
                if (userViewModel.getDataAkun().value == null)
                    authViewModel.autoRegisterToRtdb { isSuccessful ->
                        if (isSuccessful) restartApp()
                        else {
                            showToastL(getString(R.string.account_problem), requireActivity())

                            Handler(Looper.getMainLooper()).postDelayed({
                                userViewModel.clearAkunData()
                                restartApp()
                            }, 3210)
                        }
                    }
            }
        }
    }

    private fun startAuthLoginActivity(isRegistered: Boolean) {
        if (isRegistered) startActivity(Intent(context, LoginActivity::class.java))
        else startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun dataTransaksiHandler(akunModel: AkunModel?) {
        transaksiViewModel.totalTransaksi(akunModel).observe(viewLifecycleOwner) { transaksi ->
            binding.countAntrian.text = transaksi?.totalAntrian.toString()
            binding.countProses.text = transaksi?.totalProses.toString()
            binding.countSelesai.text = transaksi?.totalSelesai.toString()
        }
        transaksiViewModel.isLoading().observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                transaksiSaya.isVisible = !isLoading
                shimmerTransaksiSaya.isGone = !isLoading
                shimmerTransaksiSaya.apply { if (!isLoading) stopShimmer() else startShimmer() }

                if (isLoading) {
                    countAntrian.text = getString(R.string.default_angka)
                    countProses.text = getString(R.string.default_angka)
                    countSelesai.text = getString(R.string.default_angka)
                }
            }
        }
    }

    private fun showAlertDialog() {
        Helper.showAlertDialog(
            getString(R.string.logout_akun),
            getString(R.string.msg_logout),
            getString(R.string.logout),
            requireContext(),
            requireContext().getColor(R.color.error)
        ) {
            userViewModel.clearAkunData()
            restartApp()
        }
    }

    private fun restartApp() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}