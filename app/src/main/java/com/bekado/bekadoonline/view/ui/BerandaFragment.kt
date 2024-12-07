package com.bekado.bekadoonline.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.databinding.FragmentBerandaBinding
import com.bekado.bekadoonline.helper.Helper.calculateSpanCount
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperSort.sortProduk
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.helper.itemDecoration.HorizontalSpacing
import com.bekado.bekadoonline.view.adapter.AdapterButton
import com.bekado.bekadoonline.view.adapter.AdapterProduk
import com.bekado.bekadoonline.view.shimmer.ShimmerModel
import com.bekado.bekadoonline.view.ui.auth.LoginActivity
import com.bekado.bekadoonline.view.ui.bottomsheet.ShowProdukBottomSheet
import com.bekado.bekadoonline.view.ui.bottomsheet.SortProdukBottomSheet
import com.bekado.bekadoonline.view.ui.transaksi.KeranjangActivity
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModel
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.produk.ProdukViewModel
import com.bekado.bekadoonline.view.viewmodel.produk.ProdukViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

class BerandaFragment : Fragment() {
    private lateinit var binding: FragmentBerandaBinding
    private lateinit var adapterButton: AdapterButton
    private lateinit var adapterProduk: AdapterProduk
    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(requireActivity()) }
    private val keranjangViewModel: KeranjangViewModel by viewModels { KeranjangViewModelFactory.getInstance() }
    private val productViewModel: ProdukViewModel by viewModels { ProdukViewModelFactory.getInstance() }

    private var sortFilter = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                if (HelperConnection.isConnected(requireContext())) showProduk()
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
            if (HelperConnection.isConnected(requireContext())) showProduk()
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
        adapterProduk = AdapterProduk { produk ->
            ShowProdukBottomSheet(requireContext()).showDialog(produk, keranjangViewModel) {
                if (userViewModel.currentUser() != null && userViewModel.getDataAkun().value != null) {
                    keranjangViewModel.addDataProdukKeKeranjang(produk) { isSuccessful ->
                        if (isSuccessful) showToast("${produk.namaProduk} ditambahkan ke keranjang", requireContext())
                        else showToast("Tidak dapat menambahkan ${produk.namaProduk} ke keranjang", requireContext())
                    }
                } else startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        binding.rvProduk.adapter = adapterProduk
    }

    private fun setupViewModel() {
        productViewModel.getAllProduk().observe(viewLifecycleOwner) { dataProduk ->
            if (dataProduk != null) {
                sortProduk(dataProduk, sortFilter)
                searchProduk(dataProduk)

                adapterProduk.submitList(dataProduk)
                binding.produkKosong.isVisible = dataProduk.isEmpty()
                if (dataProduk.isEmpty()) emptyTextProduk()
            } else binding.produkKosong.visibility = View.VISIBLE
        }
        productViewModel.filterByKategori().observe(viewLifecycleOwner) { dataButton ->
            if (dataButton != null) {
                adapterButton = AdapterButton(dataButton) { button ->
                    if (button.isActive) {
                        activeCategory = button.idKategori.ifEmpty { "" }
                        showProduk()
                    }
                }
                binding.rvButtonSelector.adapter = adapterButton
            } else binding.filterNKategori.visibility = View.GONE

            if ((dataButton?.size ?: 0) < 2) binding.filterNKategori.visibility = View.GONE
        }
        productViewModel.isLoading().observe(viewLifecycleOwner) { isLoading ->
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
                val searchList = productViewModel.searchProduk(dataProduk, searchText)

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

    private fun showProduk() {
        searchClearText()

        productViewModel.getAllProduk().value?.let { dataProduk ->
            val filteredProduk = if (activeCategory.isNullOrEmpty()) dataProduk else dataProduk.filter { it.idKategori == activeCategory }
            val sortedProduk = filteredProduk.toMutableList()
            sortProduk(sortedProduk as ArrayList<ProdukModel>, sortFilter)

            adapterProduk.submitList(sortedProduk)
            searchProduk(sortedProduk)
            emptyConditionLayout(sortedProduk)
        }
    }

    private fun emptyConditionLayout(produk: List<ProdukModel>) {
        binding.produkKosong.isVisible = produk.isEmpty()
        binding.rvProduk.isGone = produk.isEmpty()
        if (produk.isEmpty()) emptyTextProduk()
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(viewLifecycleOwner) { akun ->
            binding.appBar.setOnMenuItemClickListener {
                if (akun != null) {
                    if (akun.statusAdmin) adminKeranjangState(requireContext(), it)
                    else startActivity(Intent(context, KeranjangActivity::class.java))
                } else startActivity(Intent(context, LoginActivity::class.java))
                true
            }
        }
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
        showProduk()
    }

    companion object {
        var activeCategory: String? = null
    }
}