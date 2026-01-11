package com.example.android.medicinecabinet.medicines

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineListener
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.MedicinesAdapter
import com.example.android.medicinecabinet.databinding.FragmentMedicinesBinding
import com.example.android.medicinecabinet.R

class MedicinesFragment : Fragment() {

    lateinit var binding: FragmentMedicinesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_medicines, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository =
            MedicineRepository(MedicineDatabase.getDatabase(requireContext()).medicineDao(),
                MedicineDatabase.getDatabase(requireContext()).takingTimeDao(),
                MedicineDatabase.getDatabase(requireContext()).selectedTakingDaysDao())
        val factory = MedicinesViewModelFactory(repository)
        val medicinesViewModel = ViewModelProvider(this, factory)[MedicinesViewModel::class.java]

        val adapter = MedicinesAdapter(MedicineListener { medicineId ->
            medicinesViewModel.onNavigateDetail(medicineId)
        })

        binding.rcView.adapter = adapter
        binding.rcView.layoutManager = LinearLayoutManager(context)

        binding.medicinesViewModel = medicinesViewModel
        binding.lifecycleOwner = this


        medicinesViewModel.onClickAddButton.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(R.id.action_medicinesFragment2_to_howToAddFragment2)
                medicinesViewModel.onNavigateAddDone()
            }
        })


        medicinesViewModel.allMedicines.observe(viewLifecycleOwner) {
            it?.let {
                Log.d("MedicinesFragment", "List size: ${it.size}")
                adapter.submitListByType(it, Constance.ITEM_TYPE_MEDICINE)


                it.forEachIndexed { index, medicine ->
                    Log.d("MedicinesFragment", "Position $index: $medicine")
                }
            }
        }


        medicinesViewModel.onClickDetail.observe(viewLifecycleOwner) { medicineId ->
            medicineId?.let {
                val action = MedicinesFragmentDirections.actionMedicinesFragment2ToDetailFragment22(medicineId)

                findNavController().navigate(action)
                medicinesViewModel.onNavigateDetailDone()
            }
        }
    }
}