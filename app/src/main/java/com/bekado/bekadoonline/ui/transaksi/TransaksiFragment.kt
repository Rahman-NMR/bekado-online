package com.bekado.bekadoonline.ui.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bekado.bekadoonline.LoginActivity
import com.bekado.bekadoonline.RegisterActivity
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.HelperAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class TransaksiFragment : Fragment() {
    private var _binding: FragmentTransaksiBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))


        binding.login.setOnClickListener { startActivity(Intent(context, LoginActivity::class.java)) }
        binding.register.setOnClickListener { startActivity(Intent(context, RegisterActivity::class.java)) }
        binding.logout.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                googleSignInClient.signOut()
                Toast.makeText(context, "Berhasil Logout", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(context, "Anda belum Login", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}