package com.bekado.bekadoonline.view.ui.profil

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAlamatBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.snackbarAction
import com.bekado.bekadoonline.helper.Helper.snackbarActionClose
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.viewmodel.user.AlamatViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class AlamatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlamatBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val addressViewModel: AlamatViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    private var namaAlamat: String = ""
    private var nohpAlamat: String = ""
    private var alamatLengkap: String = ""
    private var kodePosAlamat: String = ""

    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBekPressed()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        when {
            permission[ACCESS_FINE_LOCATION] ?: false || permission[ACCESS_COARSE_LOCATION] ?: false -> getLocationCoordinate()
            else -> showToast(getString(R.string.need_permission_location), this@AlamatActivity)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlamatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this@AlamatActivity, onBackInvokedCallback)
        supportActionBar?.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dataAkunHandler()
        dataAlamatHandler()

        with(binding) {
            namaAlamat.addTextChangedListener(alamatTextWatcher)
            nohpAlamat.addTextChangedListener(alamatTextWatcher)
            alamat.addTextChangedListener(alamatTextWatcher)
            kodePos.addTextChangedListener(alamatTextWatcher)

            appBar.setNavigationOnClickListener { onBekPressed() }
            btnGetTitikLokasi.setOnClickListener { if (HelperConnection.isConnected(this@AlamatActivity)) getLocationCoordinate() }
            btnSimpanPerubahan.setOnClickListener {
                if (HelperConnection.isConnected(this@AlamatActivity)) {
                    val outlineHelper = listOf(outlineNamaAlamat, outlineNohpAlamat, outlineAlamat, outlineKodePos)

                    if (outlineHelper.all { it.helperText == null }) validateAlamat()
                    else snackbarActionClose(root, getString(R.string.pastikan_no_error))
                }
            }
        }
    }

    private fun ActivityAlamatBinding.validateAlamat() {
        when {
            namaAlamat.text.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.nama_penerima)))
            nohpAlamat.text.isNullOrEmpty() ->
                snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.nomor_telepon_penerima)))

            alamat.text.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.alamat_lengkap)))
            kodePos.text.isNullOrEmpty() -> snackbarActionClose(root, getString(R.string.tidak_dapat_kosong, getString(R.string.kode_pos)))
            else -> saveAlamat()
        }
    }

    private fun ActivityAlamatBinding.saveAlamat() {
        val nama = namaAlamat.text.toString().trim()
        val noHp = nohpAlamat.text.toString().trim()
        val alamatLengkap = alamat.text.toString().trim()
        val postalCode = kodePos.text.toString().trim()

        addressViewModel.updateDataAlamat(nama, noHp, alamatLengkap, postalCode) { isSuccessful ->
            if (isSuccessful) {
                namaAlamat.clearFocus()
                nohpAlamat.clearFocus()
                alamat.clearFocus()
                kodePos.clearFocus()
                btnSimpanPerubahan.isEnabled = false

                snackbarAdress(getString(R.string.alamat_berhasil_save))
            } else showToast(getString(R.string.masalah_database), this@AlamatActivity)
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            if (akun != null) {
                when {
                    namaAlamat.isEmpty() -> binding.namaAlamat.setText(akun.nama)
                    nohpAlamat.isEmpty() -> binding.nohpAlamat.setText(akun.noHp)
                }
            } else finish()
        }
    }

    private fun dataAlamatHandler() {
        addressViewModel.getDataAlamat().observe(this) { alamat ->
            if (alamat != null) {
                namaAlamat = alamat.nama ?: ""
                nohpAlamat = alamat.noHp ?: ""
                alamatLengkap = alamat.alamatLengkap ?: ""
                kodePosAlamat = alamat.kodePos ?: ""

                if (namaAlamat.isNotEmpty()) binding.namaAlamat.setText(namaAlamat)
                if (nohpAlamat.isNotEmpty()) binding.nohpAlamat.setText(nohpAlamat)
                if (alamatLengkap.isNotEmpty()) binding.alamat.setText(alamatLengkap)
                if (kodePosAlamat.isNotEmpty()) binding.kodePos.setText(kodePosAlamat)

                val latitude = alamat.latitude ?: ""
                val longitude = alamat.longitude ?: ""

                if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                    val geoCoder = Geocoder(this@AlamatActivity, Locale.getDefault())

                    try {
                        val adress = geoCoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 10)

                        if (adress != null) binding.tvTitikLokasi.text = adress[0]?.getAddressLine(0)
                        else binding.tvTitikLokasi.text = getString(R.string.titik_lokasi)
                    } catch (_: Exception) {
                        showToast(getString(R.string.location_not_found), this)
                    }
                } else binding.tvTitikLokasi.text = getString(R.string.titik_lokasi)
            } else binding.tvTitikLokasi.text = getString(R.string.titik_lokasi)
        }
        addressViewModel.isLoading().observe(this) { isLoading ->
            with(binding) {
                btnGetTitikLokasi.isEnabled = !isLoading
                namaAlamat.isEnabled = !isLoading
                nohpAlamat.isEnabled = !isLoading
                alamat.isEnabled = !isLoading
                kodePos.isEnabled = !isLoading
                progressBar.isVisible = isLoading
            }
        }
    }

    private fun getLocationCoordinate() {
        if (checkPermission(ACCESS_FINE_LOCATION) && checkPermission(ACCESS_COARSE_LOCATION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    addressViewModel.saveLatLong(location) { isSuccessful ->
                        if (isSuccessful) snackbarAdress(getString(R.string.lokasi_sekarang_disave))
                        else showToast(getString(R.string.masalah_database), this)
                    }
                } else showToast(getString(R.string.location_not_found), this)
            }
        } else requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
    }

    private fun snackbarAdress(text: String) {
        snackbarAction(binding.root, text, "Kembali", null) { finish() }
    }

    private val alamatTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val namaInput = binding.namaAlamat.text.toString().trim { it <= ' ' }
            val noHpInput = binding.nohpAlamat.text.toString().trim { it <= ' ' }
            val alamatInput = binding.alamat.text.toString().trim { it <= ' ' }
            val kodePosInput = binding.kodePos.text.toString().trim { it <= ' ' }
            binding.btnSimpanPerubahan.isEnabled =
                namaInput.isNotEmpty() && alamatInput.isNotEmpty() && noHpInput.isNotEmpty() && kodePosInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val namaInput = binding.namaAlamat.text
            val noHpInput = binding.nohpAlamat.text
            val alamatInput = binding.alamat.text
            val kodePosInput = binding.kodePos.text

            when (s) {
                noHpInput -> {
                    binding.outlineNohpAlamat.helperText = when {
                        noHpInput.isNullOrEmpty() -> null
                        noHpInput.length < 9 -> getString(R.string.min_9_angka)
                        else -> null
                    }
                }

                alamatInput -> {
                    binding.outlineAlamat.helperText = when {
                        alamatInput.isNullOrEmpty() -> null
                        alamatInput.length < 15 -> getString(R.string.alamat_terlalu_singkat)
                        else -> null
                    }
                }

                kodePosInput -> {
                    binding.outlineKodePos.helperText = when {
                        kodePosInput.isNullOrEmpty() -> null
                        kodePosInput.toString().length < 4 -> getString(R.string.kode_pos_invalid)
                        else -> null
                    }
                }
            }

            if (namaAlamat.isNotEmpty() && nohpAlamat.isNotEmpty() && alamatLengkap.isNotEmpty() && kodePosAlamat.isNotEmpty())
                binding.btnSimpanPerubahan.isEnabled = !(namaInput.toString() == namaAlamat
                        && noHpInput.toString() == nohpAlamat
                        && alamatInput.toString() == alamatLengkap
                        && kodePosInput.toString() == kodePosAlamat)
        }
    }

    private fun onBekPressed() {
        Helper.showAlertDialog(
            getString(R.string.keluar_halaman),
            getString(R.string.msg_alamat),
            getString(R.string.keluar),
            this,
            getColor(R.color.blue_grey_700)
        ) { finish() }
    }
}