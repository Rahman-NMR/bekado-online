package com.bekado.bekadoonline.shimmer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.databinding.ShimmerTransaksiListBinding

class AdapterTransaksiShimmer(private val shimmer: ArrayList<ShimmerModel>) : RecyclerView.Adapter<AdapterTransaksiShimmer.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = ShimmerTransaksiListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = shimmer.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.data(shimmer[position])
    }

    class ViewHolder(val binding: ShimmerTransaksiListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun data(shimmer: ShimmerModel) {
            binding.shimmer.text = shimmer.count.toString()
        }
    }
}