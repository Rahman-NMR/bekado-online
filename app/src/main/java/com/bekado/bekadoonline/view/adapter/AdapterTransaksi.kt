package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bekado.bekadoonline.data.model.TransaksiModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdapterTransaksi(private var listenerTransaksi: (TransaksiModel) -> Unit) :
    ListAdapter<TransaksiModel, AdapterTransaksi.ViewHolder>(DiffCallback()) {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutTransaksiBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listenerTransaksi, context)
    }

    class ViewHolder(val binding: LayoutTransaksiBinding) : RecyclerView.ViewHolder(binding.root) {
        private val calendar = Calendar.getInstance()

        fun bind(transaksiModel: TransaksiModel, listenerTransaksi: (TransaksiModel) -> Unit, context: Context) {
            val sumPrice =
                if (transaksiModel.totalBelanja!! >= 1)
                    transaksiModel.currency + Helper.addcoma3digit(transaksiModel.totalBelanja)
                else "Gratis"
            val itemCount = "${transaksiModel.jumlahProduk} item"
            val nmrPesanan = "${context.getString(R.string.no_pesanan_)} ${transaksiModel.noPesanan}"

            val time = transaksiModel.timestamp?.toLong()
            with(binding) {
                if (time != null) {
                    calendar.timeInMillis = time
                    val waktunya = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(time))
                    tanggalPesan.text = waktunya
                } else tanggalPesan.text = context.getString(R.string.tidak_ada_data)

                noPesanan.text = nmrPesanan
                namaProduk.text = transaksiModel.namaProduk
                jumlahItem.text = itemCount
                totalHarga.text = sumPrice
                statusPesanan.text = transaksiModel.statusPesanan
                when (transaksiModel.statusPesanan) {
                    context.getString(R.string.status_menunggu_pembayaran) -> {
                        statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.outline))
                        statusPesanan.setBackgroundResource(R.drawable.background_stroke1_99)
                    }

                    context.getString(R.string.status_selesai) -> {
                        statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.done))
                        statusPesanan.setBackgroundResource(R.drawable.background_stroke1_done_99)
                    }

                    context.getString(R.string.status_dibatalkan) -> {
                        statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.error))
                        statusPesanan.setBackgroundResource(R.drawable.background_stroke1_red_99)
                    }

                    else -> {
                        statusPesanan.setTextColor(ContextCompat.getColor(context, R.color.blue_700))
                        statusPesanan.setBackgroundResource(R.drawable.background_stroke1_primary_99)
                    }
                }

                if (transaksiModel.produkLainnya!! >= 2) {
                    val moreProduk = "+${transaksiModel.produkLainnya - 1} produk lainnya"

                    produkLainnya.visibility = View.VISIBLE
                    produkLainnya.text = moreProduk
                } else produkLainnya.visibility = View.GONE
                Glide.with(root.context).load(transaksiModel.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image).into(gambarProduk)

                ll.setOnClickListener { listenerTransaksi(transaksiModel) }
            }
        }
    }

    fun onApplySearch(transaksiModelList: ArrayList<TransaksiModel>) {
        submitList(transaksiModelList)
    }

    class DiffCallback : DiffUtil.ItemCallback<TransaksiModel>() {
        override fun areItemsTheSame(oldItem: TransaksiModel, newItem: TransaksiModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TransaksiModel, newItem: TransaksiModel): Boolean {
            return oldItem == newItem
        }
    }
}