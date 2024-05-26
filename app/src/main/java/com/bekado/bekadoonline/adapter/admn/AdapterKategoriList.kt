package com.bekado.bekadoonline.adapter.admn

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.databinding.LayoutKategoriListBinding

class AdapterKategoriList(
    private var listenerKategoriList: (KategoriModel) -> Unit,
    private var listenerEdit: (KategoriModel) -> Unit,
) : ListAdapter<KategoriModel, AdapterKategoriList.ViewHolder>(DiffCallback()) {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutKategoriListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listenerKategoriList, listenerEdit, context)
    }

    class ViewHolder(val binding: LayoutKategoriListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kategori: KategoriModel, listenerKategori: (KategoriModel) -> Unit, listenerEdit: (KategoriModel) -> Unit, context: Context) {
            val jumlahProduk = "${kategori.jumlahProduk} ${context.getString(R.string.produk)} | ${kategori.produkHidden} disembunyikan"
            val imgVisibility = if (kategori.visibilitas) R.drawable.icon_outline_label_24 else R.drawable.icon_outline_label_off_24

            binding.kategoriNama.text = kategori.namaKategori
            binding.kategoriJumlahProduknya.text = jumlahProduk
            binding.visibilityIcon.setImageResource(imgVisibility)

            binding.root.setOnClickListener { listenerKategori(kategori) }
            binding.btnEditKategori.setOnClickListener { listenerEdit(kategori) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<KategoriModel>() {
        override fun areItemsTheSame(oldItem: KategoriModel, newItem: KategoriModel): Boolean {
            return oldItem.idKategori == newItem.idKategori
        }

        override fun areContentsTheSame(oldItem: KategoriModel, newItem: KategoriModel): Boolean {
            return oldItem == newItem
        }
    }
}