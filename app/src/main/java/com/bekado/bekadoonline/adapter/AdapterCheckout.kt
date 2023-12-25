package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.RvCheckoutProdukListBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AdapterCheckout(var checkoutModelList: ArrayList<CombinedKeranjangModel>) :
    RecyclerView.Adapter<AdapterCheckout.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = RvCheckoutProdukListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = checkoutModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(checkoutModelList[position])
    }

    class ViewHolder(val binding: RvCheckoutProdukListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(co: CombinedKeranjangModel) {
            val checkoutPr = co.produkModel
            val checkoutKe = co.keranjangModel
            val hargaProduk = "${checkoutPr?.currency}${addcoma3digit(checkoutPr?.hargaProduk)} × ${checkoutKe?.jumlahProduk}"
            val hargaTotal = "${checkoutPr?.currency}${addcoma3digit(checkoutKe?.jumlahProduk!! * checkoutPr?.hargaProduk!!)}"

            with(binding){
                Glide.with(root.context).load(checkoutPr.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image).into(gambarProduk)
                namaProduk.text = checkoutPr.namaProduk
                jumlahHargaProduk.text = hargaProduk
                totalHarga.text = hargaTotal
            }
        }
    }
}