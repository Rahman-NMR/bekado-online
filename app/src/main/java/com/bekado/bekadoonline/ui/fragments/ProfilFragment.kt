package com.bekado.bekadoonline.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.FragmentProfilBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.TransaksiViewModel
import com.bekado.bekadoonline.ui.activities.profil.AboutBekadoActivity
import com.bekado.bekadoonline.ui.activities.profil.AkunSayaActivity
import com.bekado.bekadoonline.ui.activities.profil.AlamatActivity
import com.bekado.bekadoonline.ui.activities.admn.KategoriListActivity
import com.bekado.bekadoonline.ui.activities.auth.LoginActivity
import com.bekado.bekadoonline.ui.activities.auth.RegisterActivity
import com.bekado.bekadoonline.ui.activities.auth.UbahPasswordActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var akunRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference

    private var adminStatus: Boolean = false
    private lateinit var akunViewModel: AkunViewModel
    private lateinit var transaksiViewModel: TransaksiViewModel
    private lateinit var signInResult: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))

        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        transaksiViewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]
        signInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataLogin = result.data?.getStringExtra(VariableConstant.ACTION_SIGN_IN_RESULT)

                if (dataLogin == VariableConstant.ACTION_REFRESH_UI) viewModelLoader()
                if (dataLogin == VariableConstant.ACTION_SIGN_OUT) akunViewModel.clearAkunData()
            }
        }

        dataAkunHandler()
        dataTransaksiHandler()

        with(binding) {
            btnLogin.setOnClickListener { signInResult.launch(Intent(context, LoginActivity::class.java)) }
            btnRegister.setOnClickListener { signInResult.launch(Intent(context, RegisterActivity::class.java)) }

            btnAkunSaya.setOnClickListener { startActivity(Intent(context, AkunSayaActivity::class.java)) }
            btnDetailAlamat.setOnClickListener { startActivity(Intent(context, AlamatActivity::class.java)) }
            btnUbahPassword.setOnClickListener { startActivity(Intent(context, UbahPasswordActivity::class.java)) }
            btnInformasiToko.setOnClickListener { startActivity(Intent(context, AboutBekadoActivity::class.java)) }
            btnLogout.setOnClickListener { showAlertDialog() }
        }
    }

    private fun dataAkunHandler() {
        viewModelLoader()

        akunViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            akunRef = db.getReference("akun/${currentUser?.uid}")
            binding.notNullLayout.visibility = if (currentUser == null) View.GONE else View.VISIBLE
            binding.nullLayout.visibility = if (currentUser == null) View.VISIBLE else View.GONE
        }
        akunViewModel.akunModel.observe(viewLifecycleOwner) { akunModel ->
            if (akunModel != null) {
                with(binding) {
                    akunSaya.visibility = View.VISIBLE
                    shimmerAkunSaya.visibility = View.GONE
                    shimmerAkunSaya.stopShimmer()

                    badgeAdmin.visibility = if (akunModel.statusAdmin) View.VISIBLE else View.GONE
                    btnAdminKategoriProduk.visibility = if (akunModel.statusAdmin) View.VISIBLE else View.GONE

                    namaProfil.text = akunModel.nama
                    emailProfil.text = akunModel.email
                    Glide.with(requireContext()).load(akunModel.fotoProfil)
                        .apply(RequestOptions()).centerCrop()
                        .placeholder(R.drawable.img_broken_image_circle).into(fotoProfil)

                    btnAdminKategoriProduk.setOnClickListener { startActivity(Intent(context, KategoriListActivity::class.java)) }
                }

                val refAdmin = if (akunModel.statusAdmin) "transaksi" else "transaksi/${akunModel.uid}"
                transaksiRef = db.getReference(refAdmin)
                adminStatus = akunModel.statusAdmin

                transaksiViewModel.loadTransaksiData(transaksiRef, akunModel.statusAdmin)
            } else {
                transaksiRef = db.getReference("transaksi")
                with(binding) {
                    akunSaya.visibility = View.GONE
                    shimmerAkunSaya.visibility = View.VISIBLE
                    shimmerAkunSaya.startShimmer()
                }
            }

        }
        akunViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == false) {
                if (auth.currentUser != null && akunViewModel.akunModel.value == null) {
                    signInResult.launch(Intent(context, RegisterActivity::class.java))
                }
            }
        }
    }

    private fun dataTransaksiHandler() {
        transaksiViewModel.totalAntrian.observe(viewLifecycleOwner) { binding.countAntrian.text = it.toString() }
        transaksiViewModel.totalProses.observe(viewLifecycleOwner) { binding.countProses.text = it.toString() }
        transaksiViewModel.totalSelesai.observe(viewLifecycleOwner) { binding.countSelesai.text = it.toString() }
        transaksiViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                if (!isLoading) {
                    transaksiSaya.visibility = View.VISIBLE
                    shimmerTransaksiSaya.visibility = View.GONE
                    shimmerTransaksiSaya.stopShimmer()
                } else {
                    transaksiSaya.visibility = View.GONE
                    shimmerTransaksiSaya.visibility = View.VISIBLE
                    shimmerTransaksiSaya.startShimmer()

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
            transaksiRef = db.getReference("transaksi")
            auth.signOut()
            googleSignInClient.signOut()
            akunViewModel.clearAkunData()
        }
    }

    private fun viewModelLoader() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()
    }

    override fun onDestroy() {
        super.onDestroy()

        akunViewModel.removeAkunListener(akunRef)
        transaksiViewModel.removeTransaksiListener(transaksiRef, adminStatus)
    }
}