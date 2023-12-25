package com.bekado.bekadoonline.adapter.admn

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutKategoriListBinding
import com.bekado.bekadoonline.helper.ItemTouchHelperListener
import com.bekado.bekadoonline.model.KategoriModel
import java.util.Collections

class AdapterKategoriList(
    private var kategoriModelLilst: ArrayList<KategoriModel>,
    private var listenerKategoriList: (KategoriModel) -> Unit,
    private var listenerEdit: (KategoriModel) -> Unit
) : RecyclerView.Adapter<AdapterKategoriList.ViewHolder>(), ItemTouchHelperListener {
    lateinit var context: Context

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(kategoriModelLilst, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(kategoriModelLilst, i, i - 1)
            }
        }

        for (i in kategoriModelLilst.indices) {
            kategoriModelLilst[i].posisi = (i + 1).toLong()
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutKategoriListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = kategoriModelLilst.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(kategoriModelLilst[position], listenerKategoriList, listenerEdit, context)
    }

    class ViewHolder(val binding: LayoutKategoriListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kategori: KategoriModel, listenerKategori: (KategoriModel) -> Unit, listenerEdit: (KategoriModel) -> Unit, context: Context) {
            val jumlahProduk = "${kategori.jumlahProduk} ${context.getString(R.string.produk)}"
            binding.kategoriNama.text = kategori.namaKategori
            binding.kategoriJumlahProduknya.text = jumlahProduk

            binding.root.setOnClickListener { listenerKategori(kategori) }
            binding.btnEditKategori.setOnClickListener { listenerEdit(kategori) }
        }
    }
}