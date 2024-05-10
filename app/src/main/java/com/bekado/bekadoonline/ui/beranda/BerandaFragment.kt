package com.bekado.bekadoonline.ui.beranda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.ui.auth.LoginActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterButton
import com.bekado.bekadoonline.adapter.AdapterProduk
import com.bekado.bekadoonline.bottomsheet.ShowProdukBottomSheet
import com.bekado.bekadoonline.bottomsheet.SortProdukBottomSheet
import com.bekado.bekadoonline.databinding.FragmentBerandaBinding
import com.bekado.bekadoonline.helper.GridSpacingItemDecoration
import com.bekado.bekadoonline.helper.Helper.calculateSpanCount
import com.bekado.bekadoonline.helper.HelperAuth
import com.bekado.bekadoonline.helper.HelperAuth.adminKeranjangState
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperProduk.getAllProduk
import com.bekado.bekadoonline.helper.HelperProduk.getFiltered
import com.bekado.bekadoonline.helper.HorizontalSpacingItemDecoration
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.model.ButtonModel
import com.bekado.bekadoonline.model.KategoriModel
import com.bekado.bekadoonline.model.ProdukModel
import com.bekado.bekadoonline.model.viewmodel.AkunViewModel
import com.bekado.bekadoonline.shimmer.ShimmerModel
import com.bekado.bekadoonline.ui.auth.RegisterActivity
import com.bekado.bekadoonline.ui.transaksi.KeranjangActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BerandaFragment : Fragment() {
    private lateinit var binding: FragmentBerandaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var adapterButton: AdapterButton
    private lateinit var adapterProduk: AdapterProduk
    private val dataButton: ArrayList<ButtonModel> = ArrayList()
    private var dataKategori: ArrayList<KategoriModel> = ArrayList()
    private val dataProduk: ArrayList<ProdukModel> = ArrayList()
    private val dataShimmer: ArrayList<ShimmerModel> = ArrayList()

    private lateinit var akunRef: DatabaseReference
    private lateinit var produkRef: DatabaseReference
    private lateinit var produkListener: ValueEventListener

    private lateinit var akunViewModel: AkunViewModel
    private lateinit var signInResult: ActivityResultLauncher<Intent>

    var sortFilter = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), HelperAuth.clientGoogle(requireContext()))
        produkRef = db.getReference("produk")
        adapterButton = AdapterButton(dataButton) {}

        akunViewModel = ViewModelProvider(requireActivity())[AkunViewModel::class.java]
        signInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataLogin = result.data?.getStringExtra(VariableConstant.signInResult)

                if (dataLogin == VariableConstant.refreshUI) {
                    viewModelLoader()
                }
            }
        }

        val paddingBottom = resources.getDimensionPixelSize(R.dimen.maxBottomdp)
        val padding = resources.getDimensionPixelSize(R.dimen.smalldp)
        val lmProduk = GridLayoutManager(context, calculateSpanCount(requireContext()))
        val lmButton = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val lmShimmer = GridLayoutManager(context, calculateSpanCount(requireContext()))

        dataAkunHandler()
        getDataAllProduk()
        searchProduk()
        fabScrollToTop()
        HelperConnection.shimmerProduk(lmShimmer, binding.rvProdukShimmer, padding, dataShimmer)

        with(binding) {
            swipeRefresh.setOnRefreshListener {
                if (HelperConnection.isConnected(requireContext())) getDataAllProduk()
                binding.swipeRefresh.isRefreshing = false
            }
            btnSort.setOnClickListener { openBottomSheetSort() }
            rvProduk.apply {
                layoutManager = lmProduk
                addItemDecoration(GridSpacingItemDecoration(lmProduk.spanCount, padding, true))
                setPadding(0, 0, 0, paddingBottom)
            }
            rvButtonSelector.apply {
                addItemDecoration(HorizontalSpacingItemDecoration(padding))
                layoutManager = lmButton
            }
        }
    }

    private fun openBottomSheetSort() {
        val sortBottomSheet = SortProdukBottomSheet(requireContext())
        sortBottomSheet.showDialog(requireContext(), sortFilter)

        sortBottomSheet.dialog.setOnDismissListener {
            sortFilter = sortBottomSheet.sortFilter
            if (HelperConnection.isConnected(requireContext())) getDataAllProduk()
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

    private fun getDataAllProduk() {
        binding.searchProduk.clearFocus()
        binding.searchProduk.setQuery("", false)

        produkListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataProduk.clear()
                dataButton.clear()

                val semua = "Semua"
                dataButton.add(ButtonModel(semua, "", true, 0))
                if (isAdded) {
                    for (item in snapshot.child("kategori").children) {
                        val namaKategori = item.child("namaKategori").value as String
                        val posisi = item.child("posisi").value as Long
                        val idKategori = item.child("idKategori").value as String
                        val kategori = ButtonModel(namaKategori, idKategori, false, posisi)

                        val visibilitas = item.child("visibilitas").value as Boolean
                        if (visibilitas) dataButton.add(kategori)
                        dataButton.sortByDescending { it.posisi }
                        dataButton.reverse()

                        adapterButton = AdapterButton(dataButton) { button ->
                            if (button.isActive) {
                                if (button.namaKategori == semua)
                                    getAllProduk(snapshot, dataProduk, dataKategori, adapterProduk, sortFilter)
                                else {
                                    getFiltered(snapshot, button.idKategori, dataProduk, adapterProduk, sortFilter)
                                    binding.produkKosong.visibility = if (dataProduk.isEmpty()) View.VISIBLE else View.GONE
                                }
                            }
                        }
                        binding.rvButtonSelector.adapter = adapterButton
                    }
                }
                adapterProduk = AdapterProduk(dataProduk) { produk ->
                    ShowProdukBottomSheet(requireContext()).showDialog(requireContext(), produk, auth, db)
                }
                getAllProduk(snapshot, dataProduk, dataKategori, adapterProduk, sortFilter)
                with(binding) {
                    rvProduk.adapter = adapterProduk
                    produkKosong.visibility = if (dataProduk.isEmpty()) View.VISIBLE else View.GONE

                    shimmerRvProduk.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    shimmerRvButtonSelector.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }

                    rvProduk.visibility = View.VISIBLE
                    rvButtonSelector.visibility = View.VISIBLE
                    llBtnSort.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                with(binding) {
                    shimmerRvProduk.startShimmer()
                    shimmerRvButtonSelector.startShimmer()
                    produkKosong.visibility = View.GONE
                    rvProduk.visibility = View.GONE
                    rvButtonSelector.visibility = View.GONE
                    llBtnSort.visibility = View.GONE
                }
            }
        }
        produkRef.addListenerForSingleValueEvent(produkListener)
    }

    private fun searchProduk() {
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
                    } else {
                        binding.rvProduk.visibility = View.VISIBLE
                        adapterProduk.onApplySearch(searchList)
                        adapterButton.onSearchProduk(dataButton.filter { it.isActive } as ArrayList<ButtonModel>)
                    }
                }

                return true
            }
        }

        binding.searchProduk.setOnQueryTextFocusChangeListener { _, focus ->
            binding.filterNKategori.visibility = if (focus) View.GONE else View.VISIBLE
        }
        binding.searchProduk.setOnQueryTextListener(search)
    }

    private fun dataAkunHandler() {
        viewModelLoader()

        akunViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            akunRef = db.getReference("akun/${currentUser?.uid}")
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
                binding.appBar.setOnMenuItemClickListener {
                    if (akunModel.statusAdmin) adminKeranjangState(requireContext(), it)
                    true
                }
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