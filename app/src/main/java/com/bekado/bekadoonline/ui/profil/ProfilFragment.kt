package com.bekado.bekadoonline.ui.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bekado.bekadoonline.LoginActivity
import com.bekado.bekadoonline.MainActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.RegisterActivity
import com.bekado.bekadoonline.databinding.FragmentProfilBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.model.AkunModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    private lateinit var akunListener: ValueEventListener
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var transaksiListener: ValueEventListener

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
        val currentUser = auth.currentUser
        akunRef = db.getReference("akun/${currentUser?.uid}")
        akunListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        getRealtimeDataAkun(currentUser)

        with(binding) {
            btnLogin.setOnClickListener { startActivity(Intent(context, LoginActivity::class.java)) }
            btnRegister.setOnClickListener { startActivity(Intent(context, RegisterActivity::class.java)) }

            btnAkunSaya.setOnClickListener { startActivity(Intent(context, AkunSayaActivity::class.java)) }
            btnDetailAlamat.setOnClickListener { startActivity(Intent(context, AlamatActivity::class.java)) }
            btnUbahPassword.setOnClickListener { Toast.makeText(requireContext(), getString(R.string.ubah_password), Toast.LENGTH_SHORT).show() }
            btnTentangKami.setOnClickListener { Toast.makeText(requireContext(), getString(R.string.tentang_kami), Toast.LENGTH_SHORT).show() }
            btnLogout.setOnClickListener { showAlertDialog() }
        }
    }

    private fun getRealtimeDataAkun(currentUser: FirebaseUser?) {
        if (currentUser != null && isAdded) {
            binding.notNullLayout.visibility = View.VISIBLE

            akunListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(AkunModel::class.java)

                    if (snapshot.exists()) {
                        binding.namaProfil.text = data!!.nama
                        binding.emailProfil.text = data.email
                        if (isAdded) {
                            Glide.with(requireContext()).load(data.fotoProfil)
                                .apply(RequestOptions()).centerCrop()
                                .into(binding.fotoProfil)
                        }
                        val refAdmin = if (data.statusAdmin) "transaksi" else "transaksi/${currentUser.uid}"
                        transaksiRef = db.getReference(refAdmin)

                        if (data.statusAdmin) {
                            binding.btnAdminKategoriProduk.visibility = View.VISIBLE
                            binding.btnAdminPengiriman.visibility = View.VISIBLE
                            binding.badgeAdmin.visibility = View.VISIBLE
                            getRealtimeDataTransaksi(true)
                        } else {
                            binding.btnAdminKategoriProduk.visibility = View.GONE
                            binding.btnAdminPengiriman.visibility = View.GONE
                            binding.badgeAdmin.visibility = View.GONE
                            getRealtimeDataTransaksi(false)
                        }
                    } else {
                        auth.signOut()
                        googleSignInClient.signOut()
                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }

                    with(binding) {
                        akunSaya.visibility = View.VISIBLE
                        shimmerAkunSaya.visibility = View.GONE
                        shimmerAkunSaya.stopShimmer()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    with(binding) {
                        akunSaya.visibility = View.GONE
                        shimmerAkunSaya.visibility = View.VISIBLE
                        shimmerAkunSaya.startShimmer()
                    }
                }
            }
            akunRef.addValueEventListener(akunListener)
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
            auth.signOut()
            googleSignInClient.signOut()
            startActivity(Intent(context, MainActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.notNullLayout.visibility = View.GONE
            binding.nullLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        akunRef.removeEventListener(akunListener)
        transaksiRef.removeEventListener(transaksiListener)
    }
}