package com.example.android.medicinecabinet.data.takingTime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.internal.illegalDecoyCallException
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Query
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.databinding.TakingTimeItemBinding
import com.example.android.medicinecabinet.utils.Constance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TakingTimeUi(
    val id: Int = System.nanoTime().toInt(),
    val time: String
)

class TakingTimeAdapter(val clickListener: TakingTimeListener? = null) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(TakingTimeDiffCallback()) {

    fun submitListByType(list: List<TakingTimeUi>, viewType: Int) {

        val displayList = list
            .map { item -> item to LocalTime.parse(item.time) }
            .sortedBy { it.second }
            .map { (item, time) ->
                TakingTimeUi(
                    id = item.id,
                    time = time.format(DateTimeFormatter.ofPattern("H:mm"))
                )
            }

        val items = when (viewType) {
            Constance.ITEM_TYPE_TIME_TAKING_EDIT ->
                displayList.map { DataItem.TimeTakingEditItem(it) }

            Constance.ITEM_TYPE_TIME_TAKING_VIEW ->
                displayList.map { DataItem.TimeTakingViewItem(it) }

            else -> emptyList()
        }

        submitList(items)
    }

    class TakingTimeEditViewHolder(private val binding: TakingTimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val textViewTime = binding.takingTime
        private val btnDeleteTime = binding.btnDeleteTime

        fun bind(takingTime: TakingTimeUi, clickListener: TakingTimeListener) {
            binding.btnDeleteTime.visibility = View.VISIBLE
            textViewTime.text = takingTime.time
            textViewTime.setOnClickListener { clickListener.onItemClick(takingTime) }
            btnDeleteTime.setOnClickListener { clickListener.onDeleteClick(takingTime) }
        }

        companion object {
            fun from(parent: ViewGroup): TakingTimeEditViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = TakingTimeItemBinding.inflate(inflater, parent, false)
                return TakingTimeEditViewHolder(binding)
            }
        }
    }

    class TakingTimeShowViewHolder(private val binding: TakingTimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val textViewItem = binding.takingTime

        fun bind(takingTime: TakingTimeUi) {
            binding.btnDeleteTime.visibility = View.GONE
            textViewItem.text = takingTime.time
        }

        companion object {
            fun from(parent: ViewGroup): TakingTimeShowViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = TakingTimeItemBinding.inflate(inflater, parent, false)
                return TakingTimeShowViewHolder(binding)
            }
        }

    }

    class TakingTimeDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean = oldItem == newItem

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TimeTakingEditItem -> Constance.ITEM_TYPE_TIME_TAKING_EDIT
            is DataItem.TimeTakingViewItem -> Constance.ITEM_TYPE_TIME_TAKING_VIEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constance.ITEM_TYPE_TIME_TAKING_EDIT -> TakingTimeEditViewHolder.from(parent)
            Constance.ITEM_TYPE_TIME_TAKING_VIEW -> TakingTimeShowViewHolder.from(parent)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is TakingTimeEditViewHolder -> {
                val timeItem = getItem(position) as DataItem.TimeTakingEditItem
                holder.bind(timeItem.takingTime, clickListener!!)
            }

            is TakingTimeShowViewHolder -> {
                val timeItem = getItem(position) as DataItem.TimeTakingViewItem
                holder.bind(timeItem.takingTime)
            }

        }

    }
}

class TakingTimeListener(
    val onItemClick: (time: TakingTimeUi) -> Unit,
    val onDeleteClick: (time: TakingTimeUi) -> Unit
)

sealed class DataItem {

    data class TimeTakingEditItem(val takingTime: TakingTimeUi) : DataItem() {
        override val id: Int = takingTime.id
    }

    data class TimeTakingViewItem(val takingTime: TakingTimeUi) : DataItem() {
        override val id: Int = takingTime.id
    }

    abstract val id: Int
}