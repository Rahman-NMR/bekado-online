package com.bekado.bekadoonline.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.databinding.LayoutTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdapterTransaksi(private var listenerTransaksi: (TrxListModel) -> Unit) :
    ListAdapter<TrxListModel, AdapterTransaksi.ViewHolder>(DiffCallback()) {
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

        fun bind(trxListModel: TrxListModel, listenerTransaksi: (TrxListModel) -> Unit, context: Context) {
            val totalBerlnja = trxListModel.totalBelanja ?: 0
            val sumPrice =
                if (totalBerlnja >= 1) trxListModel.currency + Helper.addcoma3digit(totalBerlnja)
                else "Gratis"
            val itemCount = "${trxListModel.jumlahProduk ?: 0} item"
            val nmrPesanan = "${context.getString(R.string.no_pesanan_)} ${trxListModel.noPesanan}"

            val time = trxListModel.timestamp?.toLong() ?: 0
            with(binding) {
                if (time.toInt() != 0) {
                    calendar.timeInMillis = time
                    val waktunya = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(time))
                    tanggalPesan.text = waktunya
                } else tanggalPesan.text = context.getString(R.string.tidak_ada_data)

                noPesanan.text = nmrPesanan
                namaProduk.text = trxListModel.namaProduk ?: context.getString(R.string.tidak_ada_data)
                jumlahItem.text = itemCount
                totalHarga.text = sumPrice
                statusPesanan.text = trxListModel.statusPesanan ?: context.getString(R.string.tidak_ada_data)

                when (trxListModel.statusPesanan) {
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

                val prdkLainnya = trxListModel.produkLainnya ?: 0
                if (prdkLainnya >= 2) {
                    val moreProduk = "+${prdkLainnya - 1} produk lainnya"
                    produkLainnya.text = moreProduk
                }
                produkLainnya.isVisible = prdkLainnya >= 2
                Glide.with(root.context).load(trxListModel.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(gambarProduk)

                ll.setOnClickListener { listenerTransaksi(trxListModel) }
            }
        }
    }

    fun onApplySearch(trxListModelList: ArrayList<TrxListModel>) {
        submitList(trxListModelList)
    }

    class DiffCallback : DiffUtil.ItemCallback<TrxListModel>() {
        override fun areItemsTheSame(oldItem: TrxListModel, newItem: TrxListModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TrxListModel, newItem: TrxListModel): Boolean {
            return oldItem == newItem
        }
    }
}