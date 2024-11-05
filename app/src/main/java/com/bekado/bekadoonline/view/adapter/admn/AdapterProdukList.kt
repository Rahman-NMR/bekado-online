package com.bekado.bekadoonline.view.adapter.admn

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.databinding.RvDaftarProdukBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class AdapterProdukList(
    private var listenerProduk: (ProdukModel) -> Unit,
    private var listenerChecked: (ProdukModel, isChecked: Boolean) -> Unit
) : ListAdapter<ProdukModel, AdapterProdukList.ViewHolder>(DiffCallback()) {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = RvDaftarProdukBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(getItem(position), listenerProduk, listenerChecked)
    }

    class ViewHolder(val binding: RvDaftarProdukBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(produk: ProdukModel, listenerProduk: (ProdukModel) -> Unit, listenerChecked: (ProdukModel, isChecked: Boolean) -> Unit) {
            val harga = produk.currency + addcoma3digit(produk.hargaProduk)

            with(binding) {
                Glide.with(root.context).load(produk.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(gambarProduk)
                namaProduk.text = produk.namaProduk ?: binding.root.context.getString(R.string.tidak_ada_data)
                hargaProduk.text = harga
                produkVisibility.apply {
                    setOnCheckedChangeListener(null)
                    isChecked = produk.visibility
                    setOnCheckedChangeListener { _, checked ->
                        listenerChecked(produk, checked)
                    }
                }

                btnEditProduk.setOnClickListener { listenerProduk(produk) }
            }
        }
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