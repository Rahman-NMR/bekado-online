package com.bekado.bekadoonline.view.ui.profil

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAboutBekadoBinding
import com.bekado.bekadoonline.view.viewmodel.others.AboutBekadoViewModel
import com.bekado.bekadoonline.view.viewmodel.others.ViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AboutBekadoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBekadoBinding
    private val viewModel: AboutBekadoViewModel by viewModels { ViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBekadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.appBar.setNavigationOnClickListener { finish() }
        with(binding) { showDataStore() }
    }

    private fun ActivityAboutBekadoBinding.showDataStore() {
        viewModel.getDataToko { data, isSuccess ->
            Glide.with(this@AboutBekadoActivity).load(data.foto)
                .apply(RequestOptions()).centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_error)
                .into(fotoToko)
            kontakToko.text = data.kontak?.ifEmpty { getString(R.string.tidak_ada_data) }
            alamatToko.text = data.alamat?.ifEmpty { getString(R.string.tidak_ada_data) }
            namaToko.text = data.nama?.ifEmpty { getString(R.string.tidak_ada_data) }
            jamToko.text = data.operasional?.ifEmpty { getString(R.string.tidak_ada_data) }

            layoutFotoBekado.isVisible = isSuccess
            layoutInfoBekado.isVisible = isSuccess
            progressbarFotoBekado.isVisible = !isSuccess
            progressbarInfoBekado.isVisible = !isSuccess
        }
    }
}