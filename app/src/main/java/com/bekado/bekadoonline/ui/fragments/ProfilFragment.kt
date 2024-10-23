package com.bekado.bekadoonline.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.FragmentProfilBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.ui.activities.MainActivity
import com.bekado.bekadoonline.ui.activities.admn.KategoriListActivity
import com.bekado.bekadoonline.ui.activities.auth.LoginActivity
import com.bekado.bekadoonline.ui.activities.auth.RegisterActivity
import com.bekado.bekadoonline.ui.activities.auth.UbahPasswordActivity
import com.bekado.bekadoonline.ui.activities.profil.AboutBekadoActivity
import com.bekado.bekadoonline.ui.activities.profil.AkunSayaActivity
import com.bekado.bekadoonline.ui.activities.profil.AlamatActivity
import com.bekado.bekadoonline.view.viewmodel.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding

    private val akunViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(requireActivity()) }
//    private val transaksiViewModel: TransaksiViewModel by viewModels { ViewModelFactory.getInstance(requireActivity()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataAkunHandler()
//        dataTransaksiHandler()

        with(binding) {
            btnLogin.setOnClickListener { startAuthLoginActivity(true) }
            btnRegister.setOnClickListener { startAuthLoginActivity(false) }
            btnInformasiToko.setOnClickListener { startActivity(Intent(context, AboutBekadoActivity::class.java)) }
            btnLogout.setOnClickListener { showAlertDialog() }
        }
    }

    private fun dataAkunHandler() {
        akunViewModel.getDataAkun().observe(viewLifecycleOwner) { akunModel ->
            with(binding) {
                notNullLayout.isGone = akunModel == null
                nullLayout.isVisible = akunModel == null

                badgeAdmin.isVisible = akunModel != null && akunModel.statusAdmin
                btnAdminKategoriProduk.isVisible = akunModel != null && akunModel.statusAdmin

                namaProfil.text = akunModel?.nama ?: getString(R.string.strip)
                emailProfil.text = akunModel?.email ?: getString(R.string.strip)
                Glide.with(requireContext()).load(akunModel?.fotoProfil)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image_circle).into(fotoProfil)

                btnAkunSaya.isEnabled = akunModel != null
                btnDetailAlamat.isEnabled = akunModel != null
                btnUbahPassword.isEnabled = akunModel != null
                btnAdminKategoriProduk.isEnabled = akunModel != null

                if (akunModel != null) {
                    btnAdminKategoriProduk.setOnClickListener { startActivity(Intent(context, KategoriListActivity::class.java)) }
                    btnAkunSaya.setOnClickListener { startActivity(Intent(context, AkunSayaActivity::class.java)) }
                    btnDetailAlamat.setOnClickListener { startActivity(Intent(context, AlamatActivity::class.java)) }
                    btnUbahPassword.setOnClickListener { startActivity(Intent(context, UbahPasswordActivity::class.java)) }
                }
            }

        }
        akunViewModel.isLoading().observe(viewLifecycleOwner) { isLoading ->
            binding.akunSaya.isVisible = !isLoading
            binding.shimmerAkunSaya.isVisible = isLoading
            binding.shimmerAkunSaya.apply { if (isLoading) startShimmer() else stopShimmer() }

            if (!isLoading) {
                if (akunViewModel.getDataAkun().value == null)
                    startAuthLoginActivity(false)
            }
        }
    }

    private fun startAuthLoginActivity(isRegistered: Boolean) {
        if (isRegistered) startActivity(Intent(context, LoginActivity::class.java))
        else startActivity(Intent(context, RegisterActivity::class.java))
    }

    /*private fun dataTransaksiHandler() {
        transaksiViewModel.totalAntrian.observe(viewLifecycleOwner) { binding.countAntrian.text = it.toString() }
        transaksiViewModel.totalProses.observe(viewLifecycleOwner) { binding.countProses.text = it.toString() }
        transaksiViewModel.totalSelesai.observe(viewLifecycleOwner) { binding.countSelesai.text = it.toString() }
        transaksiViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
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
    }*/

    private fun showAlertDialog() {
        Helper.showAlertDialog(
            getString(R.string.logout_akun),
            getString(R.string.msg_logout),
            getString(R.string.logout),
            requireContext(),
            requireContext().getColor(R.color.error)
        ) {
            akunViewModel.clearAkunData()

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}