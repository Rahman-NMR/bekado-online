package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.databinding.RvLayoutButtonSelectorBinding
import com.bekado.bekadoonline.model.ButtonModel

class AdapterButton(private val buttonList: ArrayList<ButtonModel>, val onClick: (ButtonModel) -> Unit) :
    RecyclerView.Adapter<AdapterButton.ButtonViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = RvLayoutButtonSelectorBinding.inflate(layoutInflater, parent, false)
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val button = buttonList[position]
        holder.buttonText.text = button.namaKategori
        holder.itemView.isActivated = button.isActive
    }

    override fun getItemCount(): Int = buttonList.size

    inner class ButtonViewHolder(val binding: RvLayoutButtonSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        val buttonText = binding.tombolActivated

        init {
            binding.root.setOnClickListener {
                val button = buttonList[adapterPosition]
                if (!button.isActive) {
                    for (i in buttonList.indices) {
                        buttonList[i].isActive = i == adapterPosition
                    }
                    onClick(button)
                    notifyDataSetChanged()
                }
            }
        }
    }
}