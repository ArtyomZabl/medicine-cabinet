package com.example.android.medicinecabinet.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.internal.illegalDecoyCallException
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.databinding.MedicineItemBinding
import com.example.android.medicinecabinet.databinding.TakingMedicineItemBinding
import com.example.android.medicinecabinet.utils.Functions.setMarginTop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MedicinesAdapter(private val clickListener: MedicineListener) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(MedicineDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitListByType(list: List<Medicine>?, viewType: Int) {
        adapterScope.launch {
            val item = when (viewType) {
                Constance.ITEM_TYPE_MEDICINE -> list?.reversed()?.map { DataItem.MedicineItem(it) }
                else -> emptyList()
            }
            withContext(Dispatchers.Main) {
                submitList(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.MedicineItem -> Constance.ITEM_TYPE_MEDICINE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constance.ITEM_TYPE_MEDICINE -> MedicineViewHolder.from(parent)
            else -> throw illegalDecoyCallException("Unknow viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is MedicineViewHolder -> {
                val medicineItem = getItem(position) as DataItem.MedicineItem
                holder.bind(medicineItem.medicine, clickListener)
            }
        }

        // Другой вариант кода, работает точно так же
        /*when (val item = getItem(position)){
            is DataItem.MedicineItem -> (holder as MedicineViewHolder).bind(item.medicine, clickListener)
            is DataItem.TakingMedicine -> (holder as TakingMedicineViewHolder).bind(item.medicine)
        }*/
    }

    class MedicineViewHolder(private val binding: MedicineItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine, clickListener: MedicineListener) = with(binding) {
            Log.d("DEBUG_EXPIRATION", "medicine.expirationDate = '${medicine.expirationDate}'")
            Log.d("DEBUG_EXPIRATION", "length = ${medicine.expirationDate?.length}")
            Log.d("DEBUG_EXPIRATION", "isBlank = ${medicine.expirationDate?.isBlank()}")
            Log.d("DEBUG_EXPIRATION", "isNullOrBlank = ${medicine.expirationDate.isNullOrBlank()}")

            textName.text = medicine.name
            textQuantity.text = medicine.quantity.toString()

            if (medicine.imagePath != null){
                binding.medicineImage.load(File(medicine.imagePath)) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder)
                }
                medicineImage.visibility = View.VISIBLE
            } else {
                medicineImage.visibility = View.GONE
            }


            if (medicine.dosage == null) {
                textDosage.visibility = View.GONE
                textDosage.setMarginTop(0)
                textDosageUnit.visibility = View.GONE
            } else {
                textDosage.setMarginTop(8)
                textDosage.visibility = View.VISIBLE
                textDosageUnit.visibility = View.VISIBLE
                textDosage.text = medicine.dosage.toString()
                textDosageUnit.text = medicine.unit.toString()
            }

            if (medicine.expirationDate.isNullOrBlank()) {
                textExpiration.setMarginTop(0)
                textExpiration.visibility = View.GONE
            } else {
                textExpiration.setMarginTop(8)
                textExpiration.visibility = View.VISIBLE
                textExpiration.text = medicine.expirationDate
            }

            binding.medicine = medicine
            binding.clickListener = clickListener
            executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MedicineViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = MedicineItemBinding.inflate(inflater, parent, false)
                return MedicineViewHolder(binding)
            }
        }
    }

    class MedicineDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean = oldItem == newItem
    }
}

class MedicineListener(val clickListener: (medicineId: Int) -> Unit) {
    fun onClick(medicine: Medicine) = clickListener(medicine.medicineId)
}

sealed class DataItem {
    data class MedicineItem(val medicine: Medicine) : DataItem() {
        override val id = medicine.medicineId
    }

    abstract val id: Int
}
