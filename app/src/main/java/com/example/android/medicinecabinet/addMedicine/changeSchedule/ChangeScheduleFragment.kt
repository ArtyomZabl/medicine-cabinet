package com.example.android.medicinecabinet.addMedicine.changeSchedule

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.databinding.FragmentAdd3ChangeDurationBinding
import com.example.android.medicinecabinet.utils.DateFormatter
import com.example.android.medicinecabinet.utils.Functions.setMarginEnd
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class ChangeScheduleFragment : Fragment() {

    lateinit var binding: FragmentAdd3ChangeDurationBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add3_change_duration,
            container,
            false
        )
        setup()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addMedicineViewModel = addMedicineViewModel
        binding.lifecycleOwner = this

        addMedicineViewModel.selectedStartTakingDate.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
            }
        }

        addMedicineViewModel.selectedEndTakingDate.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvDateEnd.text = addMedicineViewModel.selectedEndTakingDate.value
                binding.tvDateEnd.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                binding.cvDateEnd.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.cv_background_light))
                binding.cvDateEnd.setMarginEnd(8)
                binding.btnDelete.visibility = View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.deleteSelectedEndTakingDate.collect {
                    binding.tvDateEnd.text = "Нет"
                    binding.tvDateEnd.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.btn_text_color
                        )
                    )
                    binding.cvDateEnd.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    binding.cvDateEnd.setMarginEnd(0)
                    binding.btnDelete.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.onClickDateTaking.collect { dateType ->
                    when (dateType) {
                        DateType.START -> showDatePicker(dateType)
                        DateType.END -> {
                            if (addMedicineViewModel.selectedEndTakingDate.value == null) {
                                val date = DateFormatter.full(LocalDate.now().plusMonths(1))
                                addMedicineViewModel.setSelectedEndTakingDate(date)
                            } else showDatePicker(dateType)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.navBackToSchedule.collect {
                    findNavController().navigateUp()
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun showDatePicker(dateType: DateType) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = DateFormatter.numericToLong("$dayOfMonth.${month + 1}.$year")
                when (dateType) {
                    DateType.START -> addMedicineViewModel.setSelectedStartTakingDate(date)
                    DateType.END -> addMedicineViewModel.setSelectedEndTakingDate(date)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    fun setup() {
        binding.tvDateStart.text = addMedicineViewModel.setTodayDate()
    }


    enum class DateType { START, END }

}