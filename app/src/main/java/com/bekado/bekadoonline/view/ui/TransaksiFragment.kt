package com.bekado.bekadoonline.view.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.view.adapter.AdapterTransaksi
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.databinding.FragmentTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.view.shimmer.ShimmerModel
import com.bekado.bekadoonline.view.ui.auth.LoginActivity
import com.bekado.bekadoonline.view.ui.auth.RegisterActivity
import com.bekado.bekadoonline.view.ui.transaksi.DetailTransaksiActivity
import com.bekado.bekadoonline.view.ui.transaksi.DetailTransaksiActivity.Companion
import com.bekado.bekadoonline.view.ui.transaksi.KeranjangActivity
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiListViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar

class TransaksiFragment : Fragment() {
    private lateinit var binding: FragmentTransaksiBinding
    private lateinit var adapterTransaksi: AdapterTransaksi

    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(requireActivity()) }
    private val transaksiListVM: TransaksiListViewModel by viewModels { TransaksiViewModelFactory.getInstance() }
    private lateinit var detailTransaksiLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailTransaksiLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val action = result.data?.getStringExtra(VariableConstant.RESULT_ACTION)
                val noPesanan = result.data?.getStringExtra(VariableConstant.UPDATE_TRANSACTION)

                if (action == VariableConstant.REFRESH_DATA) {
                    val snackbar = Snackbar.make(binding.root, "Salin No. Pesanan", Snackbar.LENGTH_LONG)
                    snackbar.setAction("Salin") { Helper.salinPesan(requireContext(), noPesanan.toString()) }.show()
                }
            }
        }

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        val lmTransaksi = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val lmShimmer = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        dataAkunHandler()
        setupAdapter()
        HelperConnection.shimmerTransaksi(lmShimmer, binding.rvDaftarTransaksiShimmer, padding, dataShimmer)

        with(binding) {
            searchClearText()

            appBarLayout.isVisible = userViewModel.currentUser() != null
            clDaftarTransaksi.isVisible = userViewModel.currentUser() != null
            nullLayout.isGone = userViewModel.currentUser() != null

            btnLogin.setOnClickListener { startAuthLoginActivity(true) }
            btnRegister.setOnClickListener { startAuthLoginActivity(false) }

            rvDaftarTransaksi.apply {
                layoutManager = lmTransaksi
                addItemDecoration(GridSpacing(1, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
        }
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
                            data.noPesanan.toString().contains(textToSearch, ignoreCase = true) ||
                            data.statusPesanan.toString().contains(textToSearch, ignoreCase = true)
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

    private fun dataTransaksiHandler(akunModel: AkunModel?) {
        transaksiListVM.getDataTransaksi(akunModel).observe(viewLifecycleOwner) { transaksiModel ->
            if (akunModel != null) {
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
        }
        transaksiListVM.isLoading().observe(viewLifecycleOwner) { isLoading ->
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

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(viewLifecycleOwner) { akunModel ->
            dataTransaksiHandler(akunModel)

            binding.appBar.setOnMenuItemClickListener {
                if (akunModel != null) {
                    if (akunModel.statusAdmin) adminKeranjangState(requireContext(), it)
                    else startActivity(Intent(context, KeranjangActivity::class.java))
                } else startAuthLoginActivity(true)
                true
            }

            if (akunModel != null) {
                binding.swipeRefresh.setOnRefreshListener {
                    if (HelperConnection.isConnected(requireContext())) dataTransaksiHandler(akunModel)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
        userViewModel.isLoading().observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                if (userViewModel.getDataAkun().value == null)
                    startAuthLoginActivity(false)
            }
        }
    }

    private fun startAuthLoginActivity(isRegistered: Boolean) {
        if (isRegistered) startActivity(Intent(context, LoginActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        searchClearText()
    }
}