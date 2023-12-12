package com.bekado.bekadoonline.ui.profil

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAlamatBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.model.AkunModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class AlamatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlamatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var latLang: FusedLocationProviderClient

    private lateinit var alamatRef: DatabaseReference
    private lateinit var alamatListener: ValueEventListener
    private lateinit var latLongListener: ValueEventListener

    private var namaAlamat: String = ""
    private var nohpAlamat: String = ""
    private var alamatLengkap: String = ""
    private var kodePosAlamat: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlamatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        latLang = LocationServices.getFusedLocationProviderClient(this)
        val currentUser = auth.currentUser
        alamatRef = db.getReference("alamat/${currentUser?.uid}")

        getDataAlamat()
        getDataLatLong()

        with(binding) {
            namaAlamat.addTextChangedListener(alamatTextWatcher)
            nohpAlamat.addTextChangedListener(alamatTextWatcher)
            alamat.addTextChangedListener(alamatTextWatcher)
            kodePos.addTextChangedListener(alamatTextWatcher)

            appBar.setNavigationOnClickListener { onBackPressed() }
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

    private fun getDataAlamat() {
        val uidNow = auth.currentUser!!.uid
        val profilRef = db.getReference("akun/$uidNow")

        alamatListener = object : ValueEventListener {
            override fun onDataChange(snapshotAlamat: DataSnapshot) {
                if (snapshotAlamat.exists()) {
                    namaAlamat = snapshotAlamat.child("nama").value?.toString() ?: ""
                    nohpAlamat = snapshotAlamat.child("noHp").value?.toString() ?: ""
                    alamatLengkap = snapshotAlamat.child("alamatLengkap").value?.toString() ?: ""
                    kodePosAlamat = snapshotAlamat.child("kodePos").value?.toString() ?: ""

                    binding.namaAlamat.setText(namaAlamat)
                    binding.nohpAlamat.setText(nohpAlamat)
                    binding.alamat.setText(alamatLengkap)
                    binding.kodePos.setText(kodePosAlamat)
                } else {
                    profilRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshotAkun: DataSnapshot) {
                            val data = snapshotAkun.getValue(AkunModel::class.java)
                            binding.namaAlamat.setText(data?.nama.toString())
                            binding.nohpAlamat.setText(data?.noHp.toString())
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        alamatRef.addValueEventListener(alamatListener)
    }

    private fun saveAlamat() {
        val address = HashMap<String, Any>()
        address["nama"] = binding.namaAlamat.text.toString().trim()
        address["noHp"] = binding.nohpAlamat.text.toString().trim()
        address["alamatLengkap"] = binding.alamat.text.toString().trim()
        address["kodePos"] = binding.kodePos.text.toString().trim()
        alamatRef.updateChildren(address)
            .addOnSuccessListener {
                with(binding) {
                    namaAlamat.clearFocus()
                    nohpAlamat.clearFocus()
                    alamat.clearFocus()
                    kodePos.clearFocus()
                    btnSimpanPerubahan.isEnabled = false

                    val snackbar = Snackbar.make(binding.root, getString(R.string.alamat_berhasil_save), Snackbar.LENGTH_LONG)
                    snackbar.setAction("Oke") { finish() }.show()
                }
                getDataAlamat()
            }
    }

    private fun getDataLatLong() {
        latLongListener = object : ValueEventListener {
            override fun onDataChange(snapshotAlamat: DataSnapshot) {
                if (snapshotAlamat.exists()) {
                    val latitude = snapshotAlamat.child("latitude").value?.toString() ?: ""
                    val longitude = snapshotAlamat.child("longitude").value?.toString() ?: ""

                    if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                        val geoCoder = Geocoder(this@AlamatActivity, Locale.getDefault())
                        val adress = geoCoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 10)
                        val namaJalan = if (adress!![0].thoroughfare != null) adress[0].thoroughfare else ""
                        val noRumah = if (adress[0].subThoroughfare != null) "No.${adress[0].subThoroughfare}," else ""
                        val komplek = if (adress[0].subLocality != null) "${adress[0].subLocality}," else ""
                        val camatKel = if (adress[0].locality != null) "${adress[0].locality}," else ""
                        val kotaKab = if (adress[0].subAdminArea != null) "${adress[0].subAdminArea}," else ""
                        val provinsi = if (adress[0].adminArea != null) "${adress[0].adminArea}," else ""
                        val kodePos = if (adress[0].postalCode != null) adress[0].postalCode else ""

                        val fullAlamat = "$namaJalan $noRumah $komplek $camatKel $kotaKab $provinsi $kodePos"
                        binding.tvTitikLokasi.text = fullAlamat
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        alamatRef.addListenerForSingleValueEvent(latLongListener)
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
                val lat = it.latitude
                val lang = it.longitude
                val geoCoder = Geocoder(this, Locale.getDefault())
                val adress = geoCoder.getFromLocation(lat, lang, 10)
                val namaJalan = if (adress!![0].thoroughfare != null) adress[0].thoroughfare else ""
                val noRumah = if (adress[0].subThoroughfare != null) "No.${adress[0].subThoroughfare}," else ""
                val komplek = if (adress[0].subLocality != null) "${adress[0].subLocality}," else ""
                val camatKel = if (adress[0].locality != null) "${adress[0].locality}," else ""
                val kotaKab = if (adress[0].subAdminArea != null) "${adress[0].subAdminArea}," else ""
                val provinsi = if (adress[0].adminArea != null) "${adress[0].adminArea}," else ""
                val kodePos = if (adress[0].postalCode != null) adress[0].postalCode else ""

                val address = HashMap<String, Any>()
                address["latitude"] = lat.toString()
                address["longitude"] = lang.toString()
                alamatRef.updateChildren(address)
                    .addOnSuccessListener {
                        val fullAlamat = "$namaJalan $noRumah $komplek $camatKel $kotaKab $provinsi $kodePos"
                        binding.tvTitikLokasi.text = fullAlamat
                    }

//                val snackbar = Snackbar.make(binding.root, "Lokasi sudah benar?", Snackbar.LENGTH_LONG)//todo:for admin
//                snackbar.setAction("Periksa") {
//                    val gmmIntentUri = Uri.parse("geo:0,0?q=$lat,$lang")
//                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//                    mapIntent.setPackage("com.google.android.apps.maps")
//
//                    startActivity(mapIntent)
//                }.show()
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

    private fun showAlertDialog() {
        val alertdialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
            .setTitle(getString(R.string.keluar_halaman))
            .setMessage(getString(R.string.msg_alamat))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.batalkan)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.keluar)) { _, _ ->
                finish()
            }.show()

        val negativeBtn = alertdialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val positiveBtn = alertdialog.getButton(DialogInterface.BUTTON_POSITIVE)

        negativeBtn.apply {
            textSize = 16f
            setTextColor(context.getColor(R.color.grey_500))
        }

        positiveBtn.apply {
            textSize = 16f
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showAlertDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        alamatRef.removeEventListener(alamatListener)
    }
}