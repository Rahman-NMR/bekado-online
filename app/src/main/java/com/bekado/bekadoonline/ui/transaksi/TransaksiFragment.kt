package com.bekado.bekadoonline.ui.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.LoginActivity
import com.bekado.bekadoonline.MainActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.RegisterActivity
import com.bekado.bekadoonline.adapter.AdapterTransaksi
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.GridSpacingItemDecoration
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.model.AkunModel
import com.bekado.bekadoonline.shimmer.ShimmerModel
import com.bekado.bekadoonline.ui.KeranjangActivity
import com.bekado.bekadoonline.model.TransaksiModel
import com.example.testnew.utils.HelperTransaksi.getData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransaksiFragment : Fragment() {
    private lateinit var binding: FragmentTransaksiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var adapterTransaksi: AdapterTransaksi
    private var dataTransaksi: ArrayList<TransaksiModel> = ArrayList()
    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()

    private lateinit var akunRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var transaksiListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))

        val currentUser = auth.currentUser
        akunRef = db.getReference("akun/${currentUser?.uid}")

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        val lmTransaksi = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val lmShimmer = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        getRealtimeDataAkun(currentUser)
        HelperConnection.shimmerTransaksi(lmShimmer, binding.rvDaftarTransaksiShimmer, padding, dataShimmer)

        with(binding) {
            btnLogin.setOnClickListener { startActivity(Intent(context, LoginActivity::class.java)) }
            btnRegister.setOnClickListener { startActivity(Intent(context, RegisterActivity::class.java)) }

            appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_keranjang -> {
                        if (auth.currentUser != null) requireContext().startActivity(Intent(context, KeranjangActivity::class.java))
                        else requireContext().startActivity(Intent(context, LoginActivity::class.java))
                    }
                }
                true
            }
            rvDaftarTransaksi.apply {
                layoutManager = lmTransaksi
                addItemDecoration(GridSpacingItemDecoration(1, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
        }
    }

    private fun getRealtimeDataAkun(currentUser: FirebaseUser?) {
        if (currentUser != null && isAdded) {
            akunRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(AkunModel::class.java)

                    if (snapshot.exists()) {
                        val refAdmin = if (data!!.statusAdmin) "transaksi" else "transaksi/${currentUser.uid}"
                        transaksiRef = db.getReference(refAdmin)
                        if (data.statusAdmin) {
                            getTransaksiData(true)
                            searchTransaksi(true)
//                            binding.filterStatusPesanan.setOnClickListener { bottomSheetStatus(true) }
//                            binding.filterByTime.setOnClickListener { bottomSheetDate(true) }
                            binding.swipeRefresh.setOnRefreshListener {
                                if (HelperConnection.isConnected(requireContext())) getTransaksiData(true)
                                binding.swipeRefresh.isRefreshing = false
                            }
                        } else {
                            getTransaksiData(false)
                            searchTransaksi(false)
//                            binding.filterStatusPesanan.setOnClickListener { bottomSheetStatus(false) }
//                            binding.filterByTime.setOnClickListener { bottomSheetDate(false) }
                            binding.swipeRefresh.setOnRefreshListener {
                                if (HelperConnection.isConnected(requireContext())) getTransaksiData(false)
                                binding.swipeRefresh.isRefreshing = false
                            }
                        }
                    } else {
                        auth.signOut()
                        googleSignInClient.signOut()
                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }

                    binding.appBarLayout.visibility = if (snapshot.exists()) View.VISIBLE else View.GONE
                    binding.clDaftarTransaksi.visibility = if (snapshot.exists()) View.VISIBLE else View.GONE
                    binding.nullLayout.visibility = if (snapshot.exists()) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun getTransaksiData(isAdmin: Boolean) {
        binding.searchTransaksi.clearFocus()
        binding.searchTransaksi.setQuery("", false)

        transaksiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataTransaksi.clear()

                if (isAdmin) {
                    for (snap in snapshot.children) {
                        for (item in snap.children) {
                            getData(item, dataTransaksi)
                        }
                    }
                } else {
                    for (item in snapshot.children) {
                        getData(item, dataTransaksi)
                    }
                }

//                if (isAdded) filteredBy()
                adapterTransaksi = AdapterTransaksi(dataTransaksi) { trx ->
                    val intent = Intent(context, DetailTransaksiActivity::class.java)
                        .putExtra("trx", trx).putExtra("isAdmin", isAdmin)
                    startActivityForResult(intent, 884)
                }
                binding.rvDaftarTransaksi.adapter = adapterTransaksi
                binding.transaksiKosong.visibility = if (adapterTransaksi.itemCount == 0) View.VISIBLE else View.GONE

                with(binding) {
                    shimmerRvDaftarTransaksi.stopShimmer()
                    shimmerRvDaftarTransaksi.visibility = View.GONE
                    rvDaftarTransaksi.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                with(binding) {
                    shimmerRvDaftarTransaksi.startShimmer()
                    shimmerRvDaftarTransaksi.visibility = View.VISIBLE
                    rvDaftarTransaksi.visibility = View.GONE
                    transaksiKosong.visibility = View.GONE
                }
            }
        }
        transaksiRef.addListenerForSingleValueEvent(transaksiListener)
    }

    private fun searchTransaksi(isAdmin: Boolean) {
        val search = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText ?: ""
                val searchList = dataTransaksi.filter { data ->
                    val textToSearch = searchText.lowercase()
                    val isMatch = data.namaProduk.toString().contains(textToSearch, ignoreCase = true) ||
                            data.totalBelanja.toString().contains(textToSearch, ignoreCase = true) ||
                            data.noPesanan.toString().contains(textToSearch, ignoreCase = true)
                    if (!isAdmin) {
                        isMatch //|| data.namaUser.toString().contains(textToSearch, ignoreCase = true)
                    } else {
                        isMatch
                    }
                } as ArrayList<TransaksiModel>

                if (HelperConnection.isConnected(requireContext())) {
                    if (searchList.isEmpty()) {
                        binding.rvDaftarTransaksi.visibility = View.GONE
//                    Toast.makeText(context, getString(R.string.tidak_ada_data), Toast.LENGTH_SHORT).show()
                    } else {
                        binding.rvDaftarTransaksi.visibility = View.VISIBLE
                        adapterTransaksi.onApplySearch(searchList)
                    }
                }

                return true
            }
        }
        binding.searchTransaksi.setOnQueryTextListener(search)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.appBarLayout.visibility = View.GONE
            binding.clDaftarTransaksi.visibility = View.GONE
            binding.nullLayout.visibility = View.VISIBLE
        }
    }
}