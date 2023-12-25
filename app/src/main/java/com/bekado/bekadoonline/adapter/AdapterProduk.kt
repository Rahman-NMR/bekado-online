package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutProdukGridBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.model.ProdukModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.ArrayList

class AdapterProduk(
    private var produkModelList: ArrayList<ProdukModel>,
    private var listenerProduk: (ProdukModel) -> Unit
) : RecyclerView.Adapter<AdapterProduk.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutProdukGridBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = produkModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(produkModelList[position], listenerProduk)
    }

    class ViewHolder(val binding: LayoutProdukGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(produk: ProdukModel, listenerProduk: (ProdukModel) -> Unit) {
            val hargaProduk = produk.currency + addcoma3digit(produk.hargaProduk)

            Glide.with(binding.root.context).load(produk.fotoProduk)
                .apply(RequestOptions()).centerCrop()
                .placeholder(R.drawable.img_broken_image).into(binding.fotoProduk)
            binding.namaProduk.text = produk.namaProduk
            binding.hargaProduk.text = hargaProduk

            binding.produkCard.setOnClickListener { listenerProduk(produk) }
        }
    }

    fun onApplySearch(produkModelList: ArrayList<ProdukModel>) {
        this.produkModelList = produkModelList
        notifyDataSetChanged()
    }
}