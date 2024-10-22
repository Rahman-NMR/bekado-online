package com.bekado.bekadoonline.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterButton
import com.bekado.bekadoonline.adapter.AdapterProduk
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.BerandaViewModel
import com.bekado.bekadoonline.databinding.FragmentBerandaBinding
import com.bekado.bekadoonline.helper.Helper.calculateSpanCount
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperProduk
import com.bekado.bekadoonline.helper.HelperSort.sortProduk
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.helper.itemDecoration.HorizontalSpacing
import com.bekado.bekadoonline.shimmer.ShimmerModel
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.ui.activities.auth.LoginActivity
import com.bekado.bekadoonline.ui.activities.auth.RegisterActivity
import com.bekado.bekadoonline.ui.activities.transaksi.KeranjangActivity
import com.bekado.bekadoonline.ui.bottomsheet.ShowProdukBottomSheet
import com.bekado.bekadoonline.ui.bottomsheet.SortProdukBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BerandaFragment : Fragment() {
    private lateinit var binding: FragmentBerandaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var adapterButton: AdapterButton
    private lateinit var adapterProduk: AdapterProduk
    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()

    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(requireActivity()) }
    private lateinit var berandaViewModel: BerandaViewModel

    private var sortFilter = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

//        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        berandaViewModel = ViewModelProvider(requireActivity())[BerandaViewModel::class.java]

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.smalldp)
        val lmProduk = GridLayoutManager(context, calculateSpanCount(requireContext()))
        val lmButton = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val lmShimmer = GridLayoutManager(context, calculateSpanCount(requireContext()))

        dataAkunHandler()
        dataProdukHandler()
        fabScrollToTop()
        HelperConnection.shimmerProduk(lmShimmer, binding.rvProdukShimmer, padding, dataShimmer)

        with(binding) {
            swipeRefresh.setOnRefreshListener {
                if (HelperConnection.isConnected(requireContext())) sortAndFilter()
                binding.swipeRefresh.isRefreshing = false
            }
            btnSort.setOnClickListener { openBottomSheetSort() }
            rvProduk.apply {
                layoutManager = lmProduk
                addItemDecoration(GridSpacing(lmProduk.spanCount, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
            rvButtonSelector.apply {
                addItemDecoration(HorizontalSpacing(padding))
                layoutManager = lmButton
            }
        }
    }

    private fun openBottomSheetSort() {
        val sortBottomSheet = SortProdukBottomSheet(requireContext())
        sortBottomSheet.showDialog(sortFilter)

        sortBottomSheet.dialog.setOnDismissListener {
            sortFilter = sortBottomSheet.sortFilter
            if (HelperConnection.isConnected(requireContext())) sortAndFilter()
        }
    }

    private fun fabScrollToTop() {
        val fab = binding.scrollToTop
        val rvScrolling = binding.rvProduk
        var isFabVisible = false

        fab.visibility = View.INVISIBLE
        rvScrolling.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !isFabVisible) {
                    fab.show()
                    isFabVisible = true
                } else if (dy < 0 && isFabVisible) {
                    fab.hide()
                    isFabVisible = false
                }
            }
        })

        fab.setOnClickListener {
            rvScrolling.smoothScrollToPosition(0)
        }
    }

    private fun setupAdapter() {
        val currentUser = auth.currentUser
        adapterProduk = AdapterProduk { produk ->
            ShowProdukBottomSheet(requireContext()).showDialog(produk, currentUser, db) {
                if (currentUser != null) {
                    val keranjangRef = db.getReference("keranjang/${currentUser.uid}")
                    HelperProduk.addToKeranjang(produk, keranjangRef, requireContext())
                } else startAuthLoginActivity(true)
            }
        }
        binding.rvProduk.adapter = adapterProduk
    }

    private fun setupViewModel() {
        berandaViewModel.dataProduk.observe(viewLifecycleOwner) { dataProduk ->
            if (dataProduk != null) {
                sortProduk(dataProduk, sortFilter)
                searchProduk(dataProduk)

                adapterProduk.submitList(dataProduk)
                binding.produkKosong.visibility = if (dataProduk.isEmpty()) View.VISIBLE else View.GONE
                if (dataProduk.isEmpty()) emptyTextProduk()
            } else binding.produkKosong.visibility = View.VISIBLE
        }
        berandaViewModel.dataButton.observe(viewLifecycleOwner) { dataButton ->
            if (dataButton != null) {
                adapterButton = AdapterButton(dataButton) { button ->
                    if (button.isActive) {
                        activeCategory = button.idKategori.ifEmpty { "" }

                        if (button.idKategori.isEmpty()) getAllProduk()
                        else getProdukFiltered(button.idKategori)
                    }
                }
                binding.rvButtonSelector.adapter = adapterButton
            } else binding.filterNKategori.visibility = View.GONE

            if ((dataButton?.size ?: 0) < 2) binding.filterNKategori.visibility = View.GONE
        }
        berandaViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.shimmerRvProduk.startShimmer()
                binding.shimmerRvButtonSelector.startShimmer()

                binding.shimmerRvProduk.visibility = View.VISIBLE
                binding.shimmerRvButtonSelector.visibility = View.VISIBLE
            } else {
                binding.shimmerRvProduk.stopShimmer()
                binding.shimmerRvButtonSelector.stopShimmer()

                binding.shimmerRvProduk.visibility = View.GONE
                binding.shimmerRvButtonSelector.visibility = View.GONE
                binding.rvProduk.visibility = View.VISIBLE
                binding.rvButtonSelector.visibility = View.VISIBLE
                binding.llBtnSort.visibility = View.VISIBLE
            }
        }
    }

    private fun searchProduk(dataProduk: ArrayList<ProdukModel>) {
        val search = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText ?: ""
                val searchList = dataProduk.filter { data ->
                    val textToSearch = searchText.lowercase()

                    data.namaProduk.toString().contains(textToSearch, ignoreCase = true) ||
                            data.hargaProduk.toString().contains(textToSearch, ignoreCase = true)
                } as ArrayList<ProdukModel>

                if (HelperConnection.isConnected(requireContext())) {
                    if (searchList.isEmpty()) {
                        binding.rvProduk.visibility = View.GONE
                        binding.produkKosong.visibility = View.VISIBLE
                        binding.produkKosongTitle.text = getString(R.string.msg_cari_kosong)
                        binding.produkKosongDesc.text = getString(R.string.desc_cari_kosong_prdk)
                    } else {
                        binding.rvProduk.visibility = View.VISIBLE
                        binding.produkKosong.visibility = View.GONE
                        emptyTextProduk()
                        adapterProduk.onApplySearch(searchList)
                    }
                }

                return true
            }
        }

        binding.searchProduk.setOnQueryTextListener(search)
    }

    private fun getAllProduk() {
        searchClearText()

        berandaViewModel.dataProduk.value?.let {
            val sortedProduk = it.toMutableList()
            sortProduk(sortedProduk as ArrayList<ProdukModel>, sortFilter)
            adapterProduk.submitList(sortedProduk)
            searchProduk(sortedProduk)
            emptyConditionLayout(sortedProduk)
        }
    }

    private fun getProdukFiltered(idKategori: String) {
        searchClearText()

        berandaViewModel.dataProduk.value?.let { dataProduk ->
            val filteredProduk = dataProduk.filter { it.idKategori == idKategori }
            val sortedProduk = filteredProduk.toMutableList()
            sortProduk(sortedProduk as ArrayList<ProdukModel>, sortFilter)

            adapterProduk.submitList(sortedProduk)
            searchProduk(sortedProduk)
            emptyConditionLayout(sortedProduk)
        }
    }

    private fun sortAndFilter() {
        if (activeCategory.isNullOrEmpty()) getAllProduk()
        else getProdukFiltered(activeCategory!!)
    }

    private fun emptyConditionLayout(produk: List<ProdukModel>) {
        binding.produkKosong.visibility = if (produk.isEmpty()) View.VISIBLE else View.GONE
        binding.rvProduk.visibility = if (produk.isEmpty()) View.GONE else View.VISIBLE
        if (produk.isEmpty()) emptyTextProduk()
    }

    private fun dataAkunHandler() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

        akunViewModel.akunModel.observe(viewLifecycleOwner) { akunModel ->
            binding.appBar.setOnMenuItemClickListener {
                if (auth.currentUser != null) {
                    if (akunModel != null) {
                        if (akunModel.statusAdmin) adminKeranjangState(requireContext(), it)
                        else startActivity(Intent(context, KeranjangActivity::class.java))
                    }
                } else startAuthLoginActivity(true)
                true
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
        binding.searchProduk.clearFocus()
        binding.searchProduk.setQuery("", false)
    }

    private fun emptyTextProduk() {
        binding.produkKosongTitle.text = getString(R.string.msg_beranda_kosong)
        binding.produkKosongDesc.text = getString(R.string.desc_beranda_kosong)
    }

    private fun dataProdukHandler() {
        searchClearText()
        setupAdapter()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        sortAndFilter()
    }

    companion object {
        var activeCategory: String? = null
    }
}