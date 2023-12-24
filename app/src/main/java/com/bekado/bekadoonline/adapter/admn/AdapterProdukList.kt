package com.bekado.bekadoonline.adapter.admn

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.databinding.RvDaftarProdukBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.model.ProdukModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.ArrayList

class AdapterProdukList(
    private var produkModelList: ArrayList<ProdukModel>,
    private var listenerProduk: (ProdukModel) -> Unit,
    private var listenerChecked: (ProdukModel, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterProdukList.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = RvDaftarProdukBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = produkModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(produkModelList[position], listenerProduk, listenerChecked)
    }

    class ViewHolder(val binding: RvDaftarProdukBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(produk: ProdukModel, listenerProduk: (ProdukModel) -> Unit, listenerChecked: (ProdukModel, isChecked: Boolean) -> Unit) {
            val harga = produk.currency + addcoma3digit(produk.hargaProduk)

            with(binding) {
                Glide.with(root.context).load(produk.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .into(gambarProduk)
                namaProduk.text = produk.namaProduk
                hargaProduk.text = harga
                produkVisibility.isChecked = produk.visibility

                btnEditProduk.setOnClickListener { listenerProduk(produk) }
                produkVisibility.setOnCheckedChangeListener { _, checked -> listenerChecked(produk, checked) }
            }
        }
    }
}