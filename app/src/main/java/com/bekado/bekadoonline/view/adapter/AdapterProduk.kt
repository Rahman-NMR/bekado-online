package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutProdukGridBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.util.ArrayList

class AdapterProduk(private var listenerProduk: (ProdukModel) -> Unit) : ListAdapter<ProdukModel, AdapterProduk.ViewHolder>(DiffCallback()) {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutProdukGridBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(getItem(position), listenerProduk)
    }

    class ViewHolder(val binding: LayoutProdukGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(produk: ProdukModel, listenerProduk: (ProdukModel) -> Unit) {
            val hargaProduk = produk.currency + addcoma3digit(produk.hargaProduk)

            Glide.with(binding.root.context).load(produk.fotoProduk)
                .apply(RequestOptions()).centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_error)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(binding.fotoProduk)
            binding.namaProduk.text = produk.namaProduk
            binding.hargaProduk.text = hargaProduk

            binding.produkCard.setOnClickListener { listenerProduk(produk) }
        }
    }

    fun onApplySearch(produkModelList: ArrayList<ProdukModel>) {
        submitList(produkModelList)
    }

    class DiffCallback : DiffUtil.ItemCallback<ProdukModel>() {
        override fun areItemsTheSame(oldItem: ProdukModel, newItem: ProdukModel): Boolean {
            return oldItem.idProduk == newItem.idProduk
        }

        override fun areContentsTheSame(oldItem: ProdukModel, newItem: ProdukModel): Boolean {
            return oldItem == newItem
        }
    }
}