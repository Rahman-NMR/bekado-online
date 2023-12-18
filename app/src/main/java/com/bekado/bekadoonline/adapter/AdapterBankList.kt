package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.databinding.RvMetodeTransferBinding
import com.bekado.bekadoonline.model.BankModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AdapterBankList(
    private var thisList: ArrayList<BankModel>,
    private var listener: (BankModel) -> Unit
) : RecyclerView.Adapter<AdapterBankList.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = RvMetodeTransferBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = thisList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bankItem = thisList[position]
        holder.nama.text = bankItem.name
        holder.itemView.isActivated = bankItem.isActive

        Glide.with(context).load(bankItem.logoWiki)
            .apply(RequestOptions()).centerInside()
            .into(holder.logo)
    }

    inner class ViewHolder(val binding: RvMetodeTransferBinding) : RecyclerView.ViewHolder(binding.root) {
        val nama = binding.namaBank
        val logo = binding.logoBank

        init {
            binding.root.setOnClickListener {
                val button = thisList[adapterPosition]
                if (!button.isActive) {
                    for (i in thisList.indices) {
                        thisList[i].isActive = i == adapterPosition
                    }
                    listener(button)
                    notifyDataSetChanged()
                }
            }
        }
    }
}