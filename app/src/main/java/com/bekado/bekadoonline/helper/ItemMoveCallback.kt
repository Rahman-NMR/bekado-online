package com.bekado.bekadoonline.helper

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekado.bekadoonline.adapter.AdapterKategoriList
import com.bekado.bekadoonline.databinding.ActivityKategoriListBinding

class ItemMoveCallback(private val adapterKategoriList: AdapterKategoriList, private val binding: ActivityKategoriListBinding) :
    ItemTouchHelper.Callback() {
    private var startPosition = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        if (startPosition == -1) startPosition = fromPosition

        adapterKategoriList.onItemMove(fromPosition, toPosition)
        binding.btnPerbaruiPosisi.visibility = if (startPosition != toPosition) View.VISIBLE else View.GONE

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false
}
