package com.bekado.bekadoonline.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.shimmer.AdapterProdukShimmer
import com.bekado.bekadoonline.shimmer.ShimmerModel

object HelperConnection {
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork

        if (network != null) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            if (networkCapabilities != null) {
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        return false
    }

    fun shimmerProduk(lmShimmer: GridLayoutManager, rv: RecyclerView, padding: Int, dataShimmer: ArrayList<ShimmerModel>) {
        rv.layoutManager = lmShimmer
        rv.addItemDecoration(GridSpacingItemDecoration(lmShimmer.spanCount, padding, true))

        dataShimmer.add(ShimmerModel(0))
        dataShimmer.add(ShimmerModel(0))
        dataShimmer.add(ShimmerModel(0))
        dataShimmer.add(ShimmerModel(0))
        dataShimmer.add(ShimmerModel(0))
        dataShimmer.add(ShimmerModel(0))

        rv.adapter = AdapterProdukShimmer(dataShimmer)
    }
}