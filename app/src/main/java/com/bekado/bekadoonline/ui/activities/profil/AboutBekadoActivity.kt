package com.bekado.bekadoonline.ui.activities.profil

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAboutBekadoBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class AboutBekadoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBekadoBinding
    private lateinit var fs: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBekadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        fs = FirebaseFirestore.getInstance()

        binding.appBar.setNavigationOnClickListener { finish() }
        binding.loadingIndicator.visibility = View.VISIBLE
        getDataToko()
    }

    private fun getDataToko() {
        val aboutCol = fs.collection("bekado")
        aboutCol.document("aboutBekado").get().addOnSuccessListener {
            with(binding) {
                Glide.with(this@AboutBekadoActivity).load(it.get("fotoToko").toString())
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image).into(fotoToko)
                kontakToko.text = it.get("kontakPerson").toString()
                alamatToko.text = it.get("lokasiToko").toString()
                namaToko.text = it.get("namaToko").toString()
                jamToko.text = it.get("operasionalToko").toString()
                mainLayout.visibility = View.VISIBLE
                loadingIndicator.visibility = View.GONE
            }
        }.addOnFailureListener { showToast(it.toString(), this) }
    }
}