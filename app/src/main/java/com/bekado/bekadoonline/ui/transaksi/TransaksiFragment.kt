package com.bekado.bekadoonline.ui.transaksi

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.ui.auth.LoginActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.ui.auth.RegisterActivity
import com.bekado.bekadoonline.adapter.AdapterTransaksi
import com.bekado.bekadoonline.bottomsheet.FilterDateBottomSheet
import com.bekado.bekadoonline.bottomsheet.FilterStatusBottomSheet
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.GridSpacingItemDecoration
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperTransaksi
import com.bekado.bekadoonline.model.TransaksiModel
import com.bekado.bekadoonline.shimmer.ShimmerModel
import com.bekado.bekadoonline.helper.HelperTransaksi.getData
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.model.viewmodel.AkunViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

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

    private var idStatusFilter = 0
    private lateinit var namaStatusFilter: String
    private var idDateFilter = 0
    private lateinit var namaDateFilter: String

    private lateinit var akunViewModel: AkunViewModel
    private lateinit var signInResult: ActivityResultLauncher<Intent>
    private val detailTransaksiLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val action = result.data?.getStringExtra("result_action")
                val string = result.data?.getStringExtra("trxUpdate")
                if (action == "refresh_data") {
//                    getRealtimeDataAkun(auth.currentUser)
                    val snackbar = Snackbar.make(binding.root, "Status $string diperbarui", Snackbar.LENGTH_LONG)
                    snackbar.setAction("Salin") { Helper.salinPesan(requireContext(), string.toString()) }.show()
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))
        namaStatusFilter = getString(R.string.f_semua_stspsnn)
        namaDateFilter = getString(R.string.f_semua_wktutrx)

        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        signInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val dataLogin = result.data?.getStringExtra(VariableConstant.signInResult)

                if (dataLogin == VariableConstant.refreshUI) {
                    viewModelLoader()
                }
            }
        }

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        val lmTransaksi = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val lmShimmer = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        dataAkunHandler()
        HelperConnection.shimmerTransaksi(lmShimmer, binding.rvDaftarTransaksiShimmer, padding, dataShimmer)

        with(binding) {
            btnLogin.setOnClickListener { signInResult.launch(Intent(context, LoginActivity::class.java)) }
            btnRegister.setOnClickListener { signInResult.launch(Intent(context, RegisterActivity::class.java)) }

            rvDaftarTransaksi.apply {
                layoutManager = lmTransaksi
                addItemDecoration(GridSpacingItemDecoration(1, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
        }
    }

    private fun bottomSheetDate(isAdmin: Boolean) {
        val dateBottomSheet = FilterDateBottomSheet(requireContext())
        dateBottomSheet.showDialog(requireContext(), idDateFilter, namaDateFilter)

        dateBottomSheet.dialog.setOnDismissListener {
            idDateFilter = dateBottomSheet.sortFilter
            namaDateFilter = dateBottomSheet.filteredName
            updateFilterDisplay(binding.filterByTime, namaDateFilter, false)
            getTransaksiData(isAdmin)
        }
    }

    private fun bottomSheetStatus(isAdmin: Boolean) {
        val statusBottomSheet = FilterStatusBottomSheet(requireContext())
        statusBottomSheet.showDialog(requireContext(), idStatusFilter, namaStatusFilter)

        statusBottomSheet.dialog.setOnDismissListener {
            idStatusFilter = statusBottomSheet.sortFilter
            namaStatusFilter = statusBottomSheet.filteredName
            updateFilterDisplay(binding.filterStatusPesanan, namaStatusFilter, true)
            getTransaksiData(isAdmin)
        }
    }

    private fun updateFilterDisplay(binding: TextView, theText: String, isBsStatus: Boolean) {
        val setColor = if (theText == getString(R.string.f_semua_stspsnn) || theText == getString(R.string.f_semua_wktutrx))
            R.color.grey_700 else R.color.blue_700
        val bgRes = if (theText == getString(R.string.f_semua_stspsnn) || theText == getString(R.string.f_semua_wktutrx))
            R.drawable.btn_like_a_chip_selector else R.drawable.background_alpha_stroke1_99
        val textColor = ContextCompat.getColor(requireContext(), setColor)

        val drawable = binding.compoundDrawables[2].mutate()
        DrawableCompat.setTint(drawable, textColor)

        binding.setTextColor(textColor)
        if (isBsStatus) binding.text = if (theText == getString(R.string.f_semua_stspsnn)) getString(R.string.status_pesanan) else theText
        else binding.text = if (theText == getString(R.string.f_semua_wktutrx)) getString(R.string.semua_waktu) else theText
        binding.setBackgroundResource(bgRes)
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

                if (isAdded) filteredBy()
                adapterTransaksi = AdapterTransaksi(dataTransaksi) { trx ->
                    val intent = Intent(context, DetailTransaksiActivity::class.java)
                        .putExtra("trx", trx).putExtra("isAdmin", isAdmin)
                    detailTransaksiLauncher.launch(intent)
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

    private fun filteredBy() {
        if (namaStatusFilter != getString(R.string.f_semua_stspsnn)) {
            val filteredByStatus = dataTransaksi.filter { data ->
                data.statusPesanan.toString().contains(namaStatusFilter, false)
            } as ArrayList<TransaksiModel>

            dataTransaksi.clear()
            filteredByStatus.forEach { status ->
                dataTransaksi.add(status)
            }
        }

        if (namaDateFilter != getString(R.string.f_semua_wktutrx)) {
            val filteredByDate = dataTransaksi.filter { data ->
                val transactionDate = Calendar.getInstance()
                transactionDate.timeInMillis = data.timestamp!!.toLong()

                when (idDateFilter) {
                    HelperTransaksi.semua -> true
                    HelperTransaksi.day7 -> {
                        val aWeekAgo = Calendar.getInstance()
                        aWeekAgo.add(Calendar.DAY_OF_YEAR, -7)
                        transactionDate.after(aWeekAgo)
                    }

                    HelperTransaksi.day30 -> {
                        val thirtyDaysAgo = Calendar.getInstance()
                        thirtyDaysAgo.add(Calendar.DAY_OF_YEAR, -30)
                        transactionDate.after(thirtyDaysAgo)
                    }

                    HelperTransaksi.day90 -> {
                        val ninetyDaysAgo = Calendar.getInstance()
                        ninetyDaysAgo.add(Calendar.DAY_OF_YEAR, -90)
                        transactionDate.after(ninetyDaysAgo)
                    }

                    else -> false
                }
            } as ArrayList<TransaksiModel>

            dataTransaksi.clear()
            filteredByDate.forEach { result ->
                dataTransaksi.add(result)
            }
        }
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
                    if (searchList.isEmpty()) binding.rvDaftarTransaksi.visibility = View.GONE
                    else {
                        binding.rvDaftarTransaksi.visibility = View.VISIBLE
                        adapterTransaksi.onApplySearch(searchList)
                    }
                }

                return true
            }
        }
        binding.searchTransaksi.setOnQueryTextListener(search)
    }

    private fun dataAkunHandler() {
        viewModelLoader()

        akunViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            akunRef = db.getReference("akun/${currentUser?.uid}")
            binding.appBarLayout.visibility = if (currentUser != null) View.VISIBLE else View.GONE
            binding.clDaftarTransaksi.visibility = if (currentUser != null) View.VISIBLE else View.GONE
            binding.nullLayout.visibility = if (currentUser != null) View.GONE else View.VISIBLE

            binding.appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_keranjang -> {
                        if (currentUser != null) startActivity(Intent(context, KeranjangActivity::class.java))
                        else signInResult.launch(Intent(context, LoginActivity::class.java))
                    }
                }
                true
            }
        }
        akunViewModel.akunModel.observe(viewLifecycleOwner) { akunModel ->
            if (akunModel != null) {
                val refAdmin = if (akunModel.statusAdmin) "transaksi" else "transaksi/${akunModel.uid}"
                transaksiRef = db.getReference(refAdmin)

                val isAdmin = akunModel.statusAdmin

                getTransaksiData(isAdmin)
                searchTransaksi(isAdmin)

                binding.filterStatusPesanan.alpha = 1.0f
                binding.filterByTime.alpha = 1.0f

                binding.filterStatusPesanan.setOnClickListener { bottomSheetStatus(isAdmin) }
                binding.filterByTime.setOnClickListener { bottomSheetDate(isAdmin) }
                binding.swipeRefresh.setOnRefreshListener {
                    if (HelperConnection.isConnected(requireContext())) getTransaksiData(isAdmin)
                    binding.swipeRefresh.isRefreshing = false
                }
                binding.appBar.setOnMenuItemClickListener {
                    if (isAdmin) adminKeranjangState(requireContext(), it)
                    true
                }
            } else {
                binding.filterStatusPesanan.alpha = 0.3f
                binding.filterByTime.alpha = 0.3f
            }

            validateDataAkun()
        }
    }

    private fun validateDataAkun() {
        akunViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == false) {
                if (auth.currentUser != null && akunViewModel.akunModel.value == null) {
                    signInResult.launch(Intent(context, RegisterActivity::class.java))
                }
            }
        }
    }

    private fun viewModelLoader() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()
    }

    override fun onDestroy() {
        super.onDestroy()
        akunViewModel.removeAkunListener(akunRef)
    }
}