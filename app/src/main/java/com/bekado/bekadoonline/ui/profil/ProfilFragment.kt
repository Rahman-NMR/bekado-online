package com.bekado.bekadoonline.ui.profil

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
import com.bekado.bekadoonline.model.viewmodel.AkunViewModel
import com.bekado.bekadoonline.ui.adm.KategoriListActivity
import com.bekado.bekadoonline.ui.auth.LoginActivity
import com.bekado.bekadoonline.ui.auth.RegisterActivity
import com.bekado.bekadoonline.ui.auth.UbahPasswordActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var akunRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var transaksiListener: ValueEventListener

    private lateinit var akunViewModel: AkunViewModel
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
        transaksiRef = db.getReference("transaksi")
        transaksiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        signInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataLogin = result.data?.getStringExtra(VariableConstant.signInResult)

                if (dataLogin == VariableConstant.refreshUI) viewModelLoader()
                if (dataLogin == VariableConstant.signOut) akunViewModel.clearAkunData()
            }
        }

        dataAkunHandler()

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

                getRealtimeDataTransaksi(akunModel.statusAdmin)
            } else {
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

    private fun getRealtimeDataTransaksi(isAdmin: Boolean) {
        transaksiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalAntrian = 0
                var totalProses = 0
                var totalSelesai = 0

                if (isAdded) {
                    if (isAdmin) {
                        for (userTransaksi in snapshot.children) {
                            for (item in userTransaksi.children) {
                                when (item.child("parentStatus").value.toString()) {
                                    getString(R.string.key_antrian) -> totalAntrian++
                                    getString(R.string.key_proses) -> totalProses++
                                    getString(R.string.key_selesai) -> totalSelesai++
                                }
                            }
                        }
                    } else {
                        for (item in snapshot.children) {
                            when (item.child("parentStatus").value.toString()) {
                                getString(R.string.key_antrian) -> totalAntrian++
                                getString(R.string.key_proses) -> totalProses++
                                getString(R.string.key_selesai) -> totalSelesai++
                            }
                        }
                    }
                }

                with(binding) {
                    countAntrian.text = totalAntrian.toString()
                    countProses.text = totalProses.toString()
                    countSelesai.text = totalSelesai.toString()

                    transaksiSaya.visibility = View.VISIBLE
                    shimmerTransaksiSaya.visibility = View.GONE
                    shimmerTransaksiSaya.stopShimmer()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                with(binding) {
                    transaksiSaya.visibility = View.GONE
                    shimmerTransaksiSaya.visibility = View.VISIBLE
                    shimmerTransaksiSaya.startShimmer()

                    countAntrian.text = getString(R.string.default_angka)
                    countProses.text = getString(R.string.default_angka)
                    countSelesai.text = getString(R.string.default_angka)
                }
            }
        }
        transaksiRef.addValueEventListener(transaksiListener)
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
        transaksiRef.removeEventListener(transaksiListener)
    }
}