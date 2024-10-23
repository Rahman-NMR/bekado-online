package com.bekado.bekadoonline.ui.fragments

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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterTransaksi
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.TransaksiListViewModel
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperTransaksi
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.shimmer.ShimmerModel
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.ui.activities.auth.LoginActivity
import com.bekado.bekadoonline.ui.activities.auth.RegisterActivity
import com.bekado.bekadoonline.ui.activities.transaksi.DetailTransaksiActivity
import com.bekado.bekadoonline.ui.activities.transaksi.DetailTransaksiActivity.Companion
import com.bekado.bekadoonline.ui.activities.transaksi.KeranjangActivity
import com.bekado.bekadoonline.ui.bottomsheet.FilterDateBottomSheet
import com.bekado.bekadoonline.ui.bottomsheet.FilterStatusBottomSheet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class TransaksiFragment : Fragment() {
    private lateinit var binding: FragmentTransaksiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapterTransaksi: AdapterTransaksi

    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()

    private lateinit var akunRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference

    private var idStatusFilter = 0
    private lateinit var namaStatusFilter: String
    private var idDateFilter = 0
    private lateinit var namaDateFilter: String
    private var adminStatus: Boolean = false

    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(requireActivity()) }
    private lateinit var transaksiListVM: TransaksiListViewModel
    private lateinit var detailTransaksiLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        namaStatusFilter = getString(R.string.f_semua_stspsnn)
        namaDateFilter = getString(R.string.f_semua_wktutrx)

//        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        transaksiListVM = ViewModelProvider(requireActivity())[TransaksiListViewModel::class.java]
        detailTransaksiLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val action = result.data?.getStringExtra(VariableConstant.RESULT_ACTION)
                val string = result.data?.getStringExtra(VariableConstant.UPDATE_TRANSACTION)

                if (action == VariableConstant.REFRESH_DATA) {
                    dataTrxHandler()
                    val snackbar = Snackbar.make(binding.root, "Status $string diperbarui", Snackbar.LENGTH_LONG)
                    snackbar.setAction("Salin") { Helper.salinPesan(requireContext(), string.toString()) }.show()
                }
            }
        }

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        val lmTransaksi = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val lmShimmer = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        dataAkunHandler()
        dataTrxHandler()
        HelperConnection.shimmerTransaksi(lmShimmer, binding.rvDaftarTransaksiShimmer, padding, dataShimmer)

        with(binding) {
            searchClearText()

            btnLogin.setOnClickListener { startAuthLoginActivity(true) }
            btnRegister.setOnClickListener { startAuthLoginActivity(false) }

            rvDaftarTransaksi.apply {
                layoutManager = lmTransaksi
                addItemDecoration(GridSpacing(1, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
            swipeRefresh.setOnRefreshListener {
                if (HelperConnection.isConnected(requireContext())) filteredBy()
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun bottomSheetDate() {
        val dateBottomSheet = FilterDateBottomSheet(requireContext())
        dateBottomSheet.showDialog(idDateFilter, namaDateFilter)

        dateBottomSheet.dialog.setOnDismissListener {
            idDateFilter = dateBottomSheet.sortFilter
            namaDateFilter = dateBottomSheet.filteredName
            updateFilterDisplay(binding.filterByTime, namaDateFilter, false)
            filteredBy()
        }
    }

    private fun bottomSheetStatus() {
        val statusBottomSheet = FilterStatusBottomSheet(requireContext())
        statusBottomSheet.showDialog(idStatusFilter, namaStatusFilter)

        statusBottomSheet.dialog.setOnDismissListener {
            idStatusFilter = statusBottomSheet.sortFilter
            namaStatusFilter = statusBottomSheet.filteredName
            updateFilterDisplay(binding.filterStatusPesanan, namaStatusFilter, true)
            filteredBy()
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

    private fun filteredBy() {
        searchClearText()

        val transaksiList = transaksiListVM.transaksiModel.value ?: return
        val filteredList = transaksiList.filter { data ->
            val kalender = Calendar.getInstance()
            kalender.timeInMillis = data.timestamp!!.toLong()

            val isStatusMatch =
                if (namaStatusFilter != getString(R.string.f_semua_stspsnn)) {
                    data.statusPesanan.toString().contains(namaStatusFilter, false)
                } else true

            val isDateMatch = if (namaDateFilter != getString(R.string.f_semua_wktutrx)) {
                when (idDateFilter) {
                    HelperTransaksi.semua -> true
                    HelperTransaksi.day7 -> {
                        val sevenDay = Calendar.getInstance()
                        sevenDay.add(Calendar.DAY_OF_YEAR, -7)
                        kalender.after(sevenDay)
                    }

                    HelperTransaksi.day30 -> {
                        val thirtyDay = Calendar.getInstance()
                        thirtyDay.add(Calendar.DAY_OF_YEAR, -30)
                        kalender.after(thirtyDay)
                    }

                    HelperTransaksi.day90 -> {
                        val ninetyDay = Calendar.getInstance()
                        ninetyDay.add(Calendar.DAY_OF_YEAR, -90)
                        kalender.after(ninetyDay)
                    }

                    else -> false
                }
            } else true

            isStatusMatch && isDateMatch
        } as ArrayList

        adapterTransaksi.submitList(filteredList)
        searchTransaksi(filteredList)
        emptyConditionLayout(filteredList)
    }

    private fun searchTransaksi(dataTransaksi: ArrayList<TransaksiModel>) {
        val search = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText ?: ""
                val searchList = dataTransaksi.filter { data ->
                    val textToSearch = searchText.lowercase()

                    data.namaProduk.toString().contains(textToSearch, ignoreCase = true) ||
                            data.totalBelanja.toString().contains(textToSearch, ignoreCase = true) ||
                            data.noPesanan.toString().contains(textToSearch, ignoreCase = true)
                } as ArrayList<TransaksiModel>

                if (HelperConnection.isConnected(requireContext())) {
                    if (searchList.isEmpty()) {
                        binding.rvDaftarTransaksi.visibility = View.GONE
                        binding.transaksiKosong.visibility = View.VISIBLE
                        binding.transaksiKosongTitle.text = getString(R.string.msg_cari_kosong)
                        binding.transaksiKosongDesc.text = getString(R.string.desc_cari_kosong_trx)
                    } else {
                        binding.rvDaftarTransaksi.visibility = View.VISIBLE
                        binding.transaksiKosong.visibility = View.GONE
                        emptyTextTransaksi()
                        adapterTransaksi.onApplySearch(searchList)
                    }
                }

                return true
            }
        }

        binding.searchTransaksi.setOnQueryTextListener(search)
    }

    private fun setupAdapter() {
        adapterTransaksi = AdapterTransaksi { trx ->
            Companion.detailTransaksi = trx
            detailTransaksiLauncher.launch(Intent(context, DetailTransaksiActivity::class.java))
        }
        binding.rvDaftarTransaksi.adapter = adapterTransaksi
    }

    private fun setupViewModel() {
        transaksiListVM.transaksiModel.observe(viewLifecycleOwner) { transaksiModel ->
            if (transaksiModel != null) {
                adapterTransaksi.submitList(transaksiModel)
                searchTransaksi(transaksiModel)

                binding.transaksiKosong.visibility = if (transaksiModel.isEmpty()) View.VISIBLE else View.GONE
                if (transaksiModel.isEmpty()) emptyTextTransaksi()
                binding.rvDaftarTransaksi.visibility = View.VISIBLE
            } else {
                binding.rvDaftarTransaksi.visibility = View.GONE
                binding.transaksiKosong.visibility = View.VISIBLE
            }
        }
        transaksiListVM.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.shimmerRvDaftarTransaksi.startShimmer()
                binding.shimmerRvDaftarTransaksi.visibility = View.VISIBLE
            } else {
                binding.shimmerRvDaftarTransaksi.stopShimmer()
                binding.shimmerRvDaftarTransaksi.visibility = View.GONE
                binding.rvDaftarTransaksi.visibility = View.VISIBLE
            }
        }
    }

    private fun emptyConditionLayout(transaksi: ArrayList<TransaksiModel>) {
        binding.transaksiKosong.visibility = if (transaksi.isEmpty()) View.VISIBLE else View.GONE
        binding.rvDaftarTransaksi.visibility = if (transaksi.isEmpty()) View.GONE else View.VISIBLE
        if (transaksi.isEmpty()) emptyTextTransaksi()
    }

    private fun dataAkunHandler() {
//        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

        /*akunViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            akunRef = db.getReference("akun/${currentUser?.uid}")
            binding.appBarLayout.visibility = if (currentUser != null) View.VISIBLE else View.GONE
            binding.clDaftarTransaksi.visibility = if (currentUser != null) View.VISIBLE else View.GONE
            binding.nullLayout.visibility = if (currentUser != null) View.GONE else View.VISIBLE
        }*/
        akunViewModel.akunModel.observe(viewLifecycleOwner) { akunModel ->
            akunRef = db.getReference("akun/${akunModel?.uid}")
            binding.appBarLayout.isVisible = akunModel != null
            binding.clDaftarTransaksi.isVisible = akunModel != null
            binding.nullLayout.isGone = akunModel != null

            binding.appBar.setOnMenuItemClickListener {
                if (auth.currentUser != null) {
                    if (akunModel != null) {
                        if (akunModel.statusAdmin) adminKeranjangState(requireContext(), it)
                        else startActivity(Intent(context, KeranjangActivity::class.java))
                    }
                } else startAuthLoginActivity(true)
                true
            }

            val refAdmin = akunModel?.let {
                when {
                    it.statusAdmin -> "transaksi"
                    else -> "transaksi/${it.uid}"
                }
            } ?: "transaksi"
            transaksiRef = db.getReference(refAdmin)

            binding.filterStatusPesanan.alpha = if (akunModel != null) 1.0f else 0.3f
            binding.filterByTime.alpha = if (akunModel != null) 1.0f else 0.3f

            if (akunModel != null) {
                akunModel.statusAdmin
                adminStatus = akunModel.statusAdmin
                transaksiListVM.loadTransaksiData(transaksiRef, akunModel.statusAdmin)

                binding.filterStatusPesanan.setOnClickListener { bottomSheetStatus() }
                binding.filterByTime.setOnClickListener { bottomSheetDate() }
            }
        }
        akunViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                if (auth.currentUser != null && akunViewModel.akunModel.value == null) startAuthLoginActivity(false)
            }
        }
    }

    private fun startAuthLoginActivity(isLogin: Boolean) {
        if (isLogin) startActivity(Intent(context, LoginActivity::class.java))
        else startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun searchClearText() {
        binding.searchTransaksi.clearFocus()
        binding.searchTransaksi.setQuery("", false)
    }

    private fun emptyTextTransaksi() {
        binding.transaksiKosongTitle.text = getString(R.string.msg_transaksi_kosong)
        binding.transaksiKosongDesc.text = getString(R.string.desc_transaksi_kosong)
    }

    private fun dataTrxHandler() {
        setupAdapter()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        filteredBy()
    }

    override fun onDestroy() {
        super.onDestroy()
        transaksiListVM.removeTransaksiListener(transaksiRef, adminStatus)
    }
}