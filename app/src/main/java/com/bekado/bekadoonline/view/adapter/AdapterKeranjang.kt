package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            krnjng: CombinedKeranjangModel,
            itemChecked: (CombinedKeranjangModel, isChecked: Boolean) -> Unit,
            itemDelete: (CombinedKeranjangModel) -> Unit,
            itemCount: (CombinedKeranjangModel, isPlus: Boolean) -> Unit
        ) {

            val krjPrdk = krnjng.produkModel
            val krjKrnjng = krnjng.keranjangModel

            val hargaProduks = krjPrdk?.currency + addcoma3digit(krjPrdk?.hargaProduk)

            with(binding) {
                Glide.with(root.context).load(krjPrdk?.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(gambarProduk)
                namaProduk.text = krjPrdk?.namaProduk
                hargaProduk.text = hargaProduks
                hapusProduk.setOnClickListener { itemDelete(krnjng) }

                if (krjKrnjng != null) {
                    actionJumlah.visibility = View.VISIBLE
                    checkBoxSelect.visibility = View.VISIBLE

                    val jmlhPrdkKrnjg = krjKrnjng.jumlahProduk ?: 0
                    jumlahProduk.text = jmlhPrdkKrnjg.toString()
                    kurangJumlahProduk.isEnabled = jmlhPrdkKrnjg.toInt() > 1
                    tambahJumlahProduk.isEnabled = jmlhPrdkKrnjg.toInt() < 100

                    checkBoxSelect.apply {
                        setOnCheckedChangeListener(null)
                        isChecked = krjKrnjng.diPilih
                        setOnCheckedChangeListener { _, isChecked ->
                            itemChecked(krnjng, isChecked)
                        }
                    }

                    tambahJumlahProduk.setOnClickListener { itemCount(krnjng, true) }
                    kurangJumlahProduk.setOnClickListener { itemCount(krnjng, false) }
                } else {
                    llProduk.setPaddingInDp(16f, 0f, 0f, 0f)
                    actionJumlah.visibility = View.GONE
                    checkBoxSelect.visibility = View.GONE
                }
            }
        }

        private fun View.setPaddingInDp(leftDp: Float, topDp: Float, rightDp: Float, bottomDp: Float) {
            val context = this.context
            val density = context.resources.displayMetrics.density

            val leftPx = (leftDp * density).toInt()
            val topPx = (topDp * density).toInt()
            val rightPx = (rightDp * density).toInt()
            val bottomPx = (bottomDp * density).toInt()

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