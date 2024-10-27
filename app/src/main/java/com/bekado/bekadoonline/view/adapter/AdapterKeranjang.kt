package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.databinding.LayoutKeranjangListBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class AdapterKeranjang(
    private var itemChecked: (CombinedKeranjangModel, isChecked: Boolean) -> Unit,
    private var itemDelete: (CombinedKeranjangModel) -> Unit,
    private var itemCount: (CombinedKeranjangModel, isPlus: Boolean) -> Unit
) : ListAdapter<CombinedKeranjangModel, AdapterKeranjang.ViewHolder>(DiffCallback()) {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutKeranjangListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemChecked, itemDelete, itemCount)
    }

    class ViewHolder(val binding: LayoutKeranjangListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            dataModel: CombinedKeranjangModel,
            itemChecked: (CombinedKeranjangModel, isChecked: Boolean) -> Unit,
            itemDelete: (CombinedKeranjangModel) -> Unit,
            itemCount: (CombinedKeranjangModel, isPlus: Boolean) -> Unit
        ) {
            val modelProduk = dataModel.produkModel
            val modelKeranjang = dataModel.keranjangModel

            val produkPrice = modelProduk?.currency + addcoma3digit(modelProduk?.hargaProduk ?: 0)
            val produkName = modelProduk?.namaProduk ?: binding.root.context.getString(R.string.strip)

            with(binding) {
                Glide.with(root.context).load(modelProduk?.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(gambarProduk)
                namaProduk.text = produkName
                hargaProduk.text = produkPrice
                hapusProduk.setOnClickListener { itemDelete(dataModel) }

                actionJumlah.isVisible = modelKeranjang != null
                checkBoxSelect.isVisible = modelKeranjang != null

                if (modelKeranjang != null) {
                    val jmlhPrdkKrnjg = modelKeranjang.jumlahProduk ?: 0
                    jumlahProduk.text = jmlhPrdkKrnjg.toString()
                    kurangJumlahProduk.isEnabled = jmlhPrdkKrnjg.toInt() > 1
                    tambahJumlahProduk.isEnabled = jmlhPrdkKrnjg.toInt() < 100

                    checkBoxSelect.apply {
                        setOnCheckedChangeListener(null)
                        isChecked = modelKeranjang.diPilih
                        setOnCheckedChangeListener { _, isChecked ->
                            itemChecked(dataModel, isChecked)
                        }
                    }

                    tambahJumlahProduk.setOnClickListener { itemCount(dataModel, true) }
                    kurangJumlahProduk.setOnClickListener { itemCount(dataModel, false) }
                } else {
                    llProduk.setPaddingInDp(16f, 0f, 0f, 0f)
                }
            }
        }

        private fun View.setPaddingInDp(leftDp: Float, topDp: Float, rightDp: Float, bottomDp: Float) {
            val padding = binding.root.resources.displayMetrics.density

            val leftPx = (leftDp * padding).toInt()
            val topPx = (topDp * padding).toInt()
            val rightPx = (rightDp * padding).toInt()
            val bottomPx = (bottomDp * padding).toInt()

            this.setPadding(leftPx, topPx, rightPx, bottomPx)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CombinedKeranjangModel>() {
        override fun areItemsTheSame(oldItem: CombinedKeranjangModel, newItem: CombinedKeranjangModel): Boolean {
            return oldItem.produkModel?.idProduk == newItem.produkModel?.idProduk
        }

        override fun areContentsTheSame(oldItem: CombinedKeranjangModel, newItem: CombinedKeranjangModel): Boolean {
            return oldItem == newItem
        }
    }
}