package com.bekado.bekadoonline.ui.activities.profil

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAlamatBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.AlamatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class AlamatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlamatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var latLang: FusedLocationProviderClient

    private lateinit var akunRef: DatabaseReference
    private lateinit var alamatRef: DatabaseReference
    private lateinit var akunViewModel: AkunViewModel
    private lateinit var alamatViewModel: AlamatViewModel

    private var namaAlamat: String = ""
    private var nohpAlamat: String = ""
    private var alamatLengkap: String = ""
    private var kodePosAlamat: String = ""

    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBekPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlamatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this@AlamatActivity, onBackInvokedCallback)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        latLang = LocationServices.getFusedLocationProviderClient(this)

        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        alamatViewModel = ViewModelProvider(this)[AlamatViewModel::class.java]

        dataAkunHandler()

        with(binding) {
            namaAlamat.addTextChangedListener(alamatTextWatcher)
            nohpAlamat.addTextChangedListener(alamatTextWatcher)
            alamat.addTextChangedListener(alamatTextWatcher)
            kodePos.addTextChangedListener(alamatTextWatcher)

            appBar.setNavigationOnClickListener { onBekPressed() }
            btnGetTitikLokasi.setOnClickListener { if (HelperConnection.isConnected(this@AlamatActivity)) getLokasi() }
            btnSimpanPerubahan.setOnClickListener {
                if (HelperConnection.isConnected(this@AlamatActivity)) {
                    if (binding.outlineNamaAlamat.helperText == null
                        && binding.outlineNohpAlamat.helperText == null
                        && binding.outlineAlamat.helperText == null
                        && binding.outlineKodePos.helperText == null
                    ) validateAlamat()
                    else {
                        val snackbar = Snackbar.make(binding.root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
                }
            }
        }
    }

    private fun validateAlamat() {
        with(binding) {
            if (namaAlamat.text.isNullOrEmpty())
                showToast("${getString(R.string.nama_penerima)} ${getString(R.string.tidak_dapat_kosong)}", this@AlamatActivity)
            else if (nohpAlamat.text.isNullOrEmpty())
                showToast("${getString(R.string.nomor_telepon_penerima)} ${getString(R.string.tidak_dapat_kosong)}", this@AlamatActivity)
            else if (alamat.text.isNullOrEmpty())
                showToast("${getString(R.string.alamat_lengkap)} ${getString(R.string.tidak_dapat_kosong)}", this@AlamatActivity)
            else if (kodePos.text.isNullOrEmpty())
                showToast("${getString(R.string.kode_pos)} ${getString(R.string.tidak_dapat_kosong)}", this@AlamatActivity)
            else saveAlamat()
        }
    }

    private fun saveAlamat() {
        val address = HashMap<String, Any>()
        address["nama"] = binding.namaAlamat.text.toString().trim()
        address["noHp"] = binding.nohpAlamat.text.toString().trim()
        address["alamatLengkap"] = binding.alamat.text.toString().trim()
        address["kodePos"] = binding.kodePos.text.toString().trim()
        alamatRef.updateChildren(address).addOnSuccessListener {
            with(binding) {
                namaAlamat.clearFocus()
                nohpAlamat.clearFocus()
                alamat.clearFocus()
                kodePos.clearFocus()
                btnSimpanPerubahan.isEnabled = false

                val snackbar = Snackbar.make(binding.root, getString(R.string.alamat_berhasil_save), Snackbar.LENGTH_SHORT)
                snackbar.setAction("Oke") { finish() }.show()
            }
        }
    }

    private fun dataAkunHandler() {
        viewModelLoader()

        akunViewModel.currentUser.observe(this) { if (it == null) finish() }
        akunViewModel.akunModel.observe(this) { akunModel ->
            if (akunModel != null) {
                akunRef = db.getReference("akun/${akunModel.uid}")
                alamatRef = db.getReference("alamat/${akunModel.uid}")

                if (namaAlamat.isEmpty() && nohpAlamat.isEmpty()) {
                    binding.namaAlamat.setText(akunModel.nama)
                    binding.nohpAlamat.setText(akunModel.noHp)
                }
            } else {
                akunRef = db.getReference("akun")
                alamatRef = db.getReference("alamat")
            }
        }
        alamatViewModel.alamatModel.observe(this) { alamatModel ->
            if (alamatModel != null) {
                namaAlamat = alamatModel.nama ?: ""
                nohpAlamat = alamatModel.noHp ?: ""
                alamatLengkap = alamatModel.alamatLengkap ?: ""
                kodePosAlamat = alamatModel.kodePos ?: ""

                if (namaAlamat.isNotEmpty()) binding.namaAlamat.setText(namaAlamat)
                if (nohpAlamat.isNotEmpty()) binding.nohpAlamat.setText(nohpAlamat)
                binding.alamat.setText(alamatLengkap)
                binding.kodePos.setText(kodePosAlamat)

                val latitude = alamatModel.latitude ?: ""
                val longitude = alamatModel.longitude ?: ""

                if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                    val geoCoder = Geocoder(this@AlamatActivity, Locale.getDefault())
                    val adress = geoCoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 10)
                    val namaJalan = if (adress!![0].thoroughfare != null) adress[0].thoroughfare else ""
                    val noRumah = if (adress[0].subThoroughfare != null) "${adress[0].subThoroughfare}," else ""
                    val komplek = if (adress[0].subLocality != null) "${adress[0].subLocality}," else ""
                    val camatKel = if (adress[0].locality != null) "${adress[0].locality}," else ""
                    val kotaKab = if (adress[0].subAdminArea != null) "${adress[0].subAdminArea}," else ""
                    val provinsi = if (adress[0].adminArea != null) "${adress[0].adminArea}," else ""
                    val kodePos = if (adress[0].postalCode != null) adress[0].postalCode else ""

                    val fullAlamat = "$namaJalan $noRumah $komplek $camatKel $kotaKab $provinsi $kodePos"
                    binding.tvTitikLokasi.text = fullAlamat
                } else binding.tvTitikLokasi.text = getString(R.string.titik_lokasi)
            } else binding.tvTitikLokasi.text = getString(R.string.titik_lokasi)
        }
        alamatViewModel.isLoading.observe(this) { isLoading ->
            with(binding) {
                btnGetTitikLokasi.isEnabled = !isLoading
                namaAlamat.isEnabled = !isLoading
                nohpAlamat.isEnabled = !isLoading
                alamat.isEnabled = !isLoading
                kodePos.isEnabled = !isLoading
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun getLokasi() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        latLang.lastLocation.addOnSuccessListener {
            if (it != null) {
                val address = HashMap<String, Any>()
                address["latitude"] = it.latitude.toString()
                address["longitude"] = it.longitude.toString()
                alamatRef.updateChildren(address).addOnSuccessListener {
                    val snackbar = Snackbar.make(binding.root, getString(R.string.lokasi_sekarang_disave), Snackbar.LENGTH_SHORT)
                    snackbar.setAction("Oke") { finish() }.show()
                }
            }
        }
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

            if (s == noHpInput) {
                if (noHpInput.isNullOrEmpty()) binding.outlineNohpAlamat.helperText = null
                else {
                    if (noHpInput.length < 9) binding.outlineNohpAlamat.helperText = getString(R.string.min_9_angka)
                    else binding.outlineNohpAlamat.helperText = null
                }
            } else if (s == alamatInput) {
                if (alamatInput.isNullOrEmpty()) binding.outlineAlamat.helperText = null
                else {
                    if (alamatInput.toString().length < 15) binding.outlineAlamat.helperText = getString(R.string.alamat_terlalu_singkat)
                    else binding.outlineAlamat.helperText = null
                }
            } else if (s == kodePosInput) {
                if (kodePosInput.isNullOrEmpty()) binding.outlineKodePos.helperText = null
                else {
                    if (kodePosInput.toString().length < 4) binding.outlineKodePos.helperText = getString(R.string.kode_pos_invalid)
                    else binding.outlineKodePos.helperText = null
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

    private fun viewModelLoader() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()
        alamatViewModel.loadCurrentUser()
        alamatViewModel.loadAlamatData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null) {
            akunViewModel.removeAkunListener(akunRef)
            alamatViewModel.removeAlamatListener(alamatRef)
        }
    }
}