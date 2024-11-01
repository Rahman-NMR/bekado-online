package com.bekado.bekadoonline.view.ui.profil

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAboutBekadoBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class AboutBekadoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBekadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBekadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.appBar.setNavigationOnClickListener { finish() }
        with(binding) { getDataToko() }
    }

    private fun ActivityAboutBekadoBinding.getDataToko() {
        FirebaseFirestore.getInstance()
            .collection("bekado")
            .document("aboutBekado").get()
            .addOnSuccessListener {
                Glide.with(this@AboutBekadoActivity).load(it.get("fotoToko").toString())
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .into(fotoToko)
                kontakToko.text = it.get("kontakPerson").toString()
                alamatToko.text = it.get("lokasiToko").toString()
                namaToko.text = it.get("namaToko").toString()
                jamToko.text = it.get("operasionalToko").toString()

                layoutFotoBekado.visibility = View.VISIBLE
                layoutInfoBekado.visibility = View.VISIBLE
                progressbarFotoBekado.visibility = View.GONE
                progressbarInfoBekado.visibility = View.GONE
            }.addOnFailureListener {
                layoutFotoBekado.visibility = View.GONE
                layoutInfoBekado.visibility = View.GONE
                progressbarFotoBekado.visibility = View.VISIBLE
                progressbarInfoBekado.visibility = View.VISIBLE
            }
    }
}