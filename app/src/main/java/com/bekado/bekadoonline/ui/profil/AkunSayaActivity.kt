package com.bekado.bekadoonline.ui.profil

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAkunSayaBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.model.AkunModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AkunSayaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunSayaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var akunRef: DatabaseReference
    private lateinit var akunListener: ValueEventListener
    private lateinit var alamatRef: DatabaseReference
    private lateinit var alamatListener: ValueEventListener

    private var namaUser: String = ""
    private var nohpUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunSayaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        val currentUser = auth.currentUser
        akunRef = db.getReference("akun/${currentUser?.uid}")
        alamatRef = db.getReference("alamat/${currentUser?.uid}")

        getRealtimeDataAkun(currentUser)

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }
            btnEditFoto.setOnClickListener { showToast("Sedang dalam perbaikan", this@AkunSayaActivity) }

            btnEditNama.setOnClickListener {
                toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                if (!namaEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                        updateData(currentUser?.uid, "nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())
                } else focusRequest(namaEdit)
            }
            btnCancelNama.setOnClickListener { toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp) }
            namaEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                    if (!namaEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                            updateData(currentUser?.uid, "nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())

                    true
                } else false
            }

            btnEditNohp.setOnClickListener {
                toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                if (!nohpEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                        updateData(currentUser?.uid, "noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())
                } else focusRequest(nohpEdit)
            }
            btnCancelNohp.setOnClickListener { toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama) }
            nohpEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                    if (!nohpEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                            updateData(currentUser?.uid, "noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())

                    true
                } else false
            }
        }
    }

    private fun getRealtimeDataAkun(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            akunListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(AkunModel::class.java)

                    namaUser = data!!.nama.toString()
                    nohpUser = data.noHp.toString()

                    binding.namaView.text = data.nama.toString()
                    if (!data.noHp.isNullOrEmpty()) binding.nohpView.text = data.noHp.toString()
                    else binding.nohpView.text = getString(R.string.tidak_ada_data)
                    binding.emailView.text = data.email.toString()
                    if (!isDestroyed) Glide.with(this@AkunSayaActivity).load(data.fotoProfil)
                        .apply(RequestOptions()).centerCrop()
                        .into(binding.fotoProfil)

                    binding.namaEdit.setText(data.nama.toString())
                    binding.nohpEdit.setText(data.noHp.toString())

                    alamatListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val alamat = snapshot.child("alamatLengkap").value.toString()
                                val kodePos = snapshot.child("kodePos").value.toString()
                                val alamatn = "$alamat, $kodePos"

                                binding.alamatView.text = alamatn
                            } else binding.alamatView.text = getString(R.string.tidak_ada_data)

                        }

                        override fun onCancelled(error: DatabaseError) {}
                    }
                    alamatRef.addValueEventListener(alamatListener)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            akunRef.addValueEventListener(akunListener)
        }
    }

    private fun toggleEditView(
        editText: EditText,
        textView: TextView,
        editButton: MaterialButton,
        cancelButton: MaterialButton,
        disableButton: MaterialButton
    ) {
        editText.isEnabled = !editText.isEnabled
        editText.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
        textView.visibility = if (!editText.isEnabled) View.VISIBLE else View.GONE
        editButton.setIconResource(if (editText.isEnabled) R.drawable.icon_round_done_24 else R.drawable.icon_round_mode_edit_24)
        cancelButton.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
        disableButton.isEnabled = !editText.isEnabled
    }

    private fun focusRequest(editText: EditText) {
        editText.requestFocus()

        editText.setSelection(editText.text.length)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateData(uid: String?, pathDb: String, editText: EditText, string: String, value: String) {
        if (editText.text.isNotEmpty()) {
            if (pathDb == "nama" && editText.text.length > 30) showToast("$string ${getString(R.string.terlalu_panjang)}", this)
            else if (pathDb == "noHp" && editText.text.length < 9) showToast("$string ${getString(R.string.terlalu_singkat)}", this)
            else db.getReference("akun/$uid/$pathDb").setValue(value)
                .addOnSuccessListener { showToast("$string ${getString(R.string.berhasil_mengubah)}", this) }
                .addOnFailureListener { showToast("$string ${getString(R.string.gagal_mengubah)}", this) }
        } else showToast("$string ${getString(R.string.tidak_dapat_kosong)}", this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null) {
            akunRef.removeEventListener(akunListener)
            alamatRef.removeEventListener(alamatListener)
        }
    }
}