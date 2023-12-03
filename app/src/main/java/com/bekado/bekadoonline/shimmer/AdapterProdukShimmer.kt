package com.bekado.bekadoonline.shimmer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.databinding.ShimmerProdukGridBinding

class AdapterProdukShimmer(private val shimmer: ArrayList<ShimmerModel>) : RecyclerView.Adapter<AdapterProdukShimmer.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = ShimmerProdukGridBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = shimmer.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(shimmer[position])
    }

    class ViewHolder(val binding: ShimmerProdukGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(shimmer: ShimmerModel) {
            binding.shimmer.text = shimmer.count.toString()
        }

    }
}