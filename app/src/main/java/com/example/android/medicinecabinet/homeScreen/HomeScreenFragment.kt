package com.example.android.medicinecabinet.homeScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineListener
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.MedicinesAdapter
import com.example.android.medicinecabinet.databinding.FragmentHomeScreenBinding

class HomeScreenFragment : Fragment() {

    lateinit var binding: FragmentHomeScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository =
            MedicineRepository(
                MedicineDatabase.getDatabase(requireContext()).medicineDao(),
                MedicineDatabase.getDatabase(requireContext()).takingTimeDao(),
                MedicineDatabase.getDatabase(requireContext()).selectedTakingDaysDao())
        val factory = HomeScreenViewModelFactory(repository)
        val homeScreenViewModel = ViewModelProvider(this, factory)[HomeScreenViewModel::class]

        val adapter = MedicinesAdapter(MedicineListener { medicineId ->
            homeScreenViewModel.onNavigateDetail(medicineId)
        })

        binding.rcViewTakingMedicine.adapter = adapter
        binding.rcViewTakingMedicine.layoutManager = LinearLayoutManager(context)

        binding.homeScreenViewModel = homeScreenViewModel
        binding.lifecycleOwner = this


        homeScreenViewModel.allTakingMedicines.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitListByType(it, Constance.ITEM_TYPE_MEDICINE_TAKING)
            }
        }

        homeScreenViewModel.onNavigateDetail.observe(viewLifecycleOwner) { medicineId ->
            medicineId?.let {
                /*findNavController().navigate(
                    HomeScreenFragmentDirections.actionHomeScreenFragment2ToDetailFragment(
                        medicineId
                    )
                )*/
                homeScreenViewModel.onNavigateDetailDone()
            }
        }
    }
}