package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bekado.bekadoonline.model.TransaksiModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdapterTransaksi(
    private var transaksiModelList: ArrayList<TransaksiModel>,
    private var listenerTransaksi: (TransaksiModel) -> Unit,
) :
    RecyclerView.Adapter<AdapterTransaksi.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutTransaksiBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = transaksiModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transaksiModelList[position], listenerTransaksi, context)
    }

    class ViewHolder(val binding: LayoutTransaksiBinding) : RecyclerView.ViewHolder(binding.root) {
        private val calendar = Calendar.getInstance()

        fun bind(transaksiModel: TransaksiModel, listenerTransaksi: (TransaksiModel) -> Unit, context: Context) {
            val totalHarga =
                if (transaksiModel.totalBelanja!! >= 1)
                    transaksiModel.currency + Helper.addcoma3digit(transaksiModel.totalBelanja)
                else "Gratis"
            val jumlahItem = "${transaksiModel.jumlahProduk} item"

            val time = transaksiModel.timestamp?.toLong()
            if (time != null) {
                calendar.timeInMillis = time
                val waktunya = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(time))
                binding.tanggalPesan.text = waktunya
            } else binding.tanggalPesan.text = context.getString(R.string.tidak_ada_data)

            binding.noPesanan.text = transaksiModel.noPesanan
            binding.namaProduk.text = transaksiModel.namaProduk
            binding.jumlahItem.text = jumlahItem
            binding.totalHarga.text = totalHarga
            binding.statusPesanan.text = transaksiModel.statusPesanan
//            when (transaksiModel.statusPesanan) {
//                context.getString(R.string.status_menunggu_pembayaran) -> {
//                    binding.statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.white))
//                    binding.statusPesanan.backgroundTintList =
//                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_500))
//                }
//
//                context.getString(R.string.status_selesai) -> {
//                    binding.statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.white))
//                    binding.statusPesanan.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_700))
//                }
//
//                context.getString(R.string.status_dibatalkan) -> {
//                    binding.statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.white))
//                    binding.statusPesanan.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error))
//                }
//
//                else -> {
//                    binding.statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
//                    binding.statusPesanan.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_500))
//                }
//            } todo: icon & warnanya aja

            if (transaksiModel.produkLainnya!! >= 2) {
                val produkLainnya = "+${transaksiModel.produkLainnya - 1} produk lainnya"

                binding.produkLainnya.visibility = View.VISIBLE
                binding.produkLainnya.text = produkLainnya
            } else binding.produkLainnya.visibility = View.GONE
            Glide.with(binding.root.context).load(transaksiModel.fotoProduk)
                .apply(RequestOptions())
                .centerCrop().into(binding.gambarProduk)

            binding.ll.setOnClickListener { listenerTransaksi(transaksiModel) }
        }
    }

    fun onApplySearch(transaksiModelList: ArrayList<TransaksiModel>) {
        this.transaksiModelList = transaksiModelList
        notifyDataSetChanged()
    }
}