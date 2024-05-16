package com.bekado.bekadoonline.helper.itemDecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpacing(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager
        if (layoutManager is LinearLayoutManager && layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
            val position = parent.getChildAdapterPosition(view)

            if (position < state.itemCount - 1) {
                outRect.right = spacing
            }
        }
    }
}
