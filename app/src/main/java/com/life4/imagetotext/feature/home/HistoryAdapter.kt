package com.life4.imagetotext.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.life4.imagetotext.R
import com.life4.imagetotext.databinding.ItemHistoryBinding
import com.life4.imagetotext.model.ResultModel

class HistoryAdapter(
    private val listener: (ResultModel) -> Unit,
    private val longClickListener: (Long) -> Unit
) :
    ListAdapter<ResultModel, HistoryAdapter.HistoryViewHolder>(DIFF_UTIL) {

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val listener: (ResultModel) -> Unit,
        private val longClickListener: (Long) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResultModel) {
            binding.item = item
            binding.root.setOnClickListener {
                listener(item)
            }
            binding.root.setOnLongClickListener {
                longClickListener(item.id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = DataBindingUtil.inflate<ItemHistoryBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_history, parent, false
        )
        return HistoryViewHolder(binding, listener, longClickListener)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ResultModel>() {
            override fun areItemsTheSame(oldItem: ResultModel, newItem: ResultModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ResultModel, newItem: ResultModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}