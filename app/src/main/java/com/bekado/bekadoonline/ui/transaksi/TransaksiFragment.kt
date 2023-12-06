package com.bekado.bekadoonline.ui.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bekado.bekadoonline.LoginActivity
import com.bekado.bekadoonline.MainActivity
import com.bekado.bekadoonline.RegisterActivity
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransaksiFragment : Fragment() {
    private lateinit var binding: FragmentTransaksiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))
        db = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        getRealtimeDataAkun(currentUser)

        binding.btnLogin.setOnClickListener { startActivity(Intent(context, LoginActivity::class.java)) }
        binding.btnRegister.setOnClickListener { startActivity(Intent(context, RegisterActivity::class.java)) }
    }

    private fun getRealtimeDataAkun(currentUser: FirebaseUser?) {
        if (currentUser != null && isAdded) {
            val akunRef = db.getReference("akun/${currentUser.uid}")
            akunRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        auth.signOut()
                        googleSignInClient.signOut()
                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }

//                    binding.notNullLayout.visibility = if (snapshot.exists()) View.VISIBLE else View.GONE
                    binding.nullLayout.visibility = if (snapshot.exists()) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
//            binding.notNullLayout.visibility = View.GONE
            binding.nullLayout.visibility = View.VISIBLE
        }
    }
}