package com.example.android.medicinecabinet.addMedicine.nameAndDosage

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import coil.load
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.utils.DateFormatter
import com.example.android.medicinecabinet.databinding.FragmentAdd1MedicineBinding
import com.example.android.medicinecabinet.utils.Functions.setMarginBottom
import kotlinx.coroutines.launch
import java.util.Calendar

class NameFragment : Fragment() {

    lateinit var binding: FragmentAdd1MedicineBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add1_medicine, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addMedicineViewModel = addMedicineViewModel
        binding.lifecycleOwner = this
        binding.textExpiration.text = ""

        addMedicineViewModel.navToDosage.observe(viewLifecycleOwner) {
            it?.let {
                addMedicineViewModel.apply {
                    textName.value = binding.textName.text.toString().trim()
                    textQuantity.value = binding.textQuantity.text.toString().toIntOrNull()
                    textExpiration.value = binding.textExpiration.text.toString().trim()
                }
                findNavController().navigate(R.id.action_nameFragment2_to_dosageFragment)
                addMedicineViewModel.navNextToDosageDone()
                Log.d("Saving", "Saved ${addMedicineViewModel.textName.value}")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.product.collect {
                    it?.let { product ->
                        addMedicineViewModel.textName.value = product.name.toString().trim()
                        Log.d("IMAGE_URL", product.imageUrl ?: "null")
                        binding.medsImage.load(product.imageUrl) {
                            crossfade(true)
                            placeholder(R.drawable.placeholder)
                        }
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.openDatePicker.collect {
                    val calendar = Calendar.getInstance()

                    val datePicker = DatePickerDialog(
                        requireContext(),
                        { _, year, month, dayOfMonth ->
                            val date = DateFormatter.numericToLong("$dayOfMonth.${month + 1}.$year")
                            addMedicineViewModel.setSelectedDate(date)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                }
            }
        }

        var initialBottomMargin = binding.button.marginBottom
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

            val lp = binding.button.layoutParams as ConstraintLayout.LayoutParams
            Log.d("initialBottomMargin", "initialBottomMargin - $initialBottomMargin")
            lp.bottomMargin = initialBottomMargin + offset
            binding.button.layoutParams = lp

            insets
        }

        fun validateFields() {
            binding.apply {
                val isNameValid = textName.text?.isNotBlank() == true
                val isQuantityValid = textQuantity.text?.isNotBlank() == true
                val allValid = isNameValid && isQuantityValid

                binding.button.isEnabled = allValid
            }
        }

        binding.textName.doOnTextChanged { _, _, _, _ -> validateFields() }
        binding.textQuantity.doOnTextChanged { _, _, _, _ -> validateFields() }
    }
}