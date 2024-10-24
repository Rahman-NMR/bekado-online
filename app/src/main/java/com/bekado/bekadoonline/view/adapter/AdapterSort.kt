package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutTextSelectedBinding
import com.bekado.bekadoonline.data.model.SortModel

class AdapterSort(
    private var sortList: ArrayList<SortModel>,
    private var listenerSort: (SortModel) -> Unit
) : RecyclerView.Adapter<AdapterSort.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutTextSelectedBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = sortList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sortItem = sortList[position]
        holder.bind(sortItem, listenerSort)

        holder.binding.root.isActivated = sortItem.dipilih
        if (sortItem.dipilih) {
            holder.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.icon_baseline_radio_button_checked_24, 0
            )
        } else {
            holder.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.icon_baseline_radio_button_unchecked_24, 0
            )
        }
    }

    class ViewHolder(val binding: LayoutTextSelectedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sortItem: SortModel, listenerSort: (SortModel) -> Unit) {
            binding.root.apply {
                text = sortItem.nama
                setOnClickListener { listenerSort(sortItem) }
            }
        }
    }
}