package com.example.consumerapp.itemDecoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewItemDecoration(val context: Context) : RecyclerView.ItemDecoration(){
    companion object{
        private const val OFFSET = 20f
    }
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = (context.resources.displayMetrics.density * OFFSET).toInt()
    }
}