package com.example.android.medicinecabinet.addMedicine.howToAdd

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.databinding.FragmentHowToAddBinding
import kotlin.getValue

class HowToAddFragment : Fragment() {

    lateinit var binding: FragmentHowToAddBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_how_to_add, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMedicineViewModel.resetAddMedicineState()

        binding.btnManual.setOnClickListener {
            findNavController().navigate(R.id.action_howToAddFragment2_to_addMedicineFragment2)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCamera.setOnClickListener {
            findNavController().navigate(R.id.action_howToAddFragment2_to_cameraFragment)
        }

    }
}