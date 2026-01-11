package com.example.android.medicinecabinet.addMedicine.dosage


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.databinding.FragmentAdd2DosageBinding
import com.example.android.medicinecabinet.utils.Functions.setMarginBottom
import kotlinx.coroutines.launch


class DosageFragment : Fragment() {

    lateinit var binding: FragmentAdd2DosageBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add2_dosage, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_dropdown, addMedicineViewModel.units)
        binding.autoCompleteDosageUnit.setAdapter(adapter)

        binding.addMedicineViewModel = addMedicineViewModel
        binding.lifecycleOwner = this

        Log.d("Saving", "Still saved ${addMedicineViewModel.textName.value}")

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.apply {
                    onClickNext.collect {
                        textDosage.value = binding.textDosage.text.toString().toFloatOrNull()

                        findNavController().navigate(R.id.action_dosageFragment_to_addScheduleFragment)
                    }
                }
            }
        }

        fun validateFields() {
            val isTextDosageValid = binding.textDosage.text?.isNotBlank() == true
            val isSelectedUnitValid =
                addMedicineViewModel.selectedUnit.value?.isNotBlank() == true
            val allValid = isSelectedUnitValid && isTextDosageValid

            binding.btnNext.isEnabled = allValid
        }

        binding.textDosage.doOnTextChanged { _, _, _, _ -> validateFields() }
        binding.autoCompleteDosageUnit.doOnTextChanged { text, _, _, _ ->
            addMedicineViewModel.selectedUnit.value = text?.toString()?.trim()
            validateFields()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.onClickSkip.collect {
                    addMedicineViewModel.textDosage.value = null
                    addMedicineViewModel.selectedUnit.value = null
                    findNavController().navigate(R.id.action_dosageFragment_to_addScheduleFragment)
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        var initialBottomMargin = binding.btnSkip.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            //Получение высоты клавиатуры
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeHeight = imeInsets.bottom

            // Получение высоты BottomNavigationView
            val bottomNavHeight =
                requireActivity()
                    .findViewById<View>(R.id.mainBottomNavigation)
                    ?.height ?: 0

            val offset = (imeHeight - bottomNavHeight).coerceAtLeast(0)

            val lp = binding.btnSkip.layoutParams as ConstraintLayout.LayoutParams
            Log.d("initialBottomMargin", "initialBottomMargin - $initialBottomMargin")
            lp.bottomMargin = initialBottomMargin + offset
            binding.btnSkip.layoutParams = lp

            insets
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter =
            ArrayAdapter(requireContext(), R.layout.list_item_dropdown, addMedicineViewModel.units)
        binding.autoCompleteDosageUnit.setAdapter(adapter)
    }

}