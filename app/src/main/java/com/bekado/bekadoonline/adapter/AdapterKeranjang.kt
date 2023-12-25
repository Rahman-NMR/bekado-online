package com.bekado.bekadoonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.LayoutKeranjangListBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.HelperProduk.plusMinus
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdapterKeranjang(
    private var keranjangModelList: ArrayList<CombinedKeranjangModel>,
    private var listenerKeranjang: (CombinedKeranjangModel, isChecked: Boolean) -> Unit,
    private var listener: (CombinedKeranjangModel) -> Unit
) : RecyclerView.Adapter<AdapterKeranjang.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = LayoutKeranjangListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = keranjangModelList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(keranjangModelList[position], listenerKeranjang, listener, context)
    }

    class ViewHolder(val binding: LayoutKeranjangListBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var db: FirebaseDatabase
        lateinit var auth: FirebaseAuth

        fun bind(
            krnjng: CombinedKeranjangModel,
            listnrChck: (CombinedKeranjangModel, isChecked: Boolean) -> Unit,
            listnrDlte: (CombinedKeranjangModel) -> Unit,
            context: Context
        ) {
            auth = FirebaseAuth.getInstance()
            db = FirebaseDatabase.getInstance()

            val krjPrdk = krnjng.produkModel
            val krjKrnjng = krnjng.keranjangModel

            val hargaProduks = krjPrdk?.currency + addcoma3digit(krjPrdk?.hargaProduk)

            with(binding) {
                Glide.with(root.context).load(krjPrdk?.fotoProduk)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image).into(gambarProduk)
                namaProduk.text = krjPrdk?.namaProduk
                hargaProduk.text = hargaProduks
                hapusProduk.setOnClickListener { listnrDlte(krnjng) }

                if (krjKrnjng != null) {
                    actionJumlah.visibility = View.VISIBLE
                    checkBoxSelect.visibility = View.VISIBLE

                    jumlahProduk.text = krjKrnjng.jumlahProduk.toString()
                    kurangJumlahProduk.isEnabled = krjKrnjng.jumlahProduk!!.toInt() != 1
                    tambahJumlahProduk.isEnabled = krjKrnjng.jumlahProduk.toInt() != 100

                    checkBoxSelect.apply {
                        setOnCheckedChangeListener(null)
                        isChecked = krjKrnjng.diPilih
                        setOnCheckedChangeListener { _, isChecked ->
                            listnrChck(krnjng, isChecked)
                        }
                    }

                    val keranjangRef = db.getReference("keranjang/${auth.currentUser!!.uid}/${krjPrdk?.idProduk}")
                    tambahJumlahProduk.setOnClickListener {
                        if (HelperConnection.isConnected(context)) plusMinus(keranjangRef, true)
                    }
                    kurangJumlahProduk.setOnClickListener {
                        if (HelperConnection.isConnected(context)) plusMinus(keranjangRef, false)
                    }
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
}