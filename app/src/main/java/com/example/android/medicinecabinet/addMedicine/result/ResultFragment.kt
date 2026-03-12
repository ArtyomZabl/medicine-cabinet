package com.example.android.medicinecabinet.addMedicine.result

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.data.takingTime.TakingTimeAdapter
import com.example.android.medicinecabinet.databinding.FragmentAdd4ResultBinding
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.utils.Functions.setMarginTop
import kotlinx.coroutines.launch
import androidx.navigation.NavOptions
import androidx.navigation.navGraphViewModels
import coil.load
import com.example.android.medicinecabinet.utils.Functions.setMarginStart
import com.example.android.medicinecabinet.utils.WeekDay
import java.io.File

class ResultFragment : Fragment() {
    lateinit var binding: FragmentAdd4ResultBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add4_result, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        addMedicineViewModel.selectedDays.value?.forEach {
            Log.d("SelectedDays", "${addMedicineViewModel.selectedDays.value}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        when (addMedicineViewModel.selectedIntakeInterval.value) {
            "По мере необходимости" -> {
                binding.constraintLayoutDuration.visibility = View.GONE
                binding.constraintLayoutSchedule.visibility = View.GONE
            }

            "Каждый день" -> {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE
                binding.constraintLayoutSchedule.setMarginTop(16)

                binding.layoutWeekDays.setMarginTop(0)
                binding.layoutWeekDays.visibility = View.GONE

                binding.tvIntakeDays.text = "Каждый день"
                binding.tvIntakeDays.visibility = View.VISIBLE
                binding.tvIntakeDays.setMarginTop(8)

                isTakingTimes()

                binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                setupEndTakingDate()
                binding.constraintLayoutDuration.visibility = View.VISIBLE
            }

            "В определённые дни" -> {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE

                binding.layoutWeekDays.visibility = View.VISIBLE
                binding.layoutWeekDays.setMarginTop(8)

                binding.tvIntakeDays.visibility = View.GONE
                binding.tvIntakeDays.setMarginTop(8)

                isTakingTimes()

                binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                setupEndTakingDate()

                binding.constraintLayoutDuration.visibility = View.VISIBLE
            }

            "Раз в несколько дней" -> {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE

                binding.layoutWeekDays.visibility = View.GONE
                binding.layoutWeekDays.setMarginTop(0)

                isTakingTimes()

                binding.tvIntakeDays.text =
                    addMedicineViewModel.formatInterval(addMedicineViewModel.daysInterval.value)

                binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                setupEndTakingDate()
            }

            else -> binding.constraintLayoutDuration.visibility = View.GONE
        }

        val adapter = TakingTimeAdapter()

        binding.addMedicineViewModel = addMedicineViewModel

        binding.rcViewTakingTime.adapter = adapter
        binding.rcViewTakingTime.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.product.collect { product ->
                    product?.let {
                        binding.medImage.load(product.imageUrl) {
                            crossfade(true)
                            placeholder(R.drawable.placeholder)
                    }
                        binding.medImage.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.tvName.text = addMedicineViewModel.textName.value
        binding.tvQuantity.text = addMedicineViewModel.textQuantity.value.toString()

        if (addMedicineViewModel.textDosage.value == null) {
            binding.tvDosage.visibility = View.GONE
            binding.tvDosage.setMarginTop(0)
            binding.tvUnit.visibility = View.GONE
        } else {
            binding.tvDosage.text = addMedicineViewModel.textDosage.value.toString()
            binding.tvUnit.text = addMedicineViewModel.selectedUnit.value
        }


        addMedicineViewModel.takingTimes.observe(viewLifecycleOwner) { allTimes ->
            adapter.submitListByType(allTimes, Constance.ITEM_TYPE_TIME_TAKING_VIEW)
        }


        addMedicineViewModel.selectedDays.observe(viewLifecycleOwner) { days ->
            val map = mapOf(
                WeekDay.MON to binding.dayMon,
                WeekDay.TUE to binding.dayTue,
                WeekDay.WED to binding.dayWed,
                WeekDay.THU to binding.dayThu,
                WeekDay.FRI to binding.dayFri,
                WeekDay.SAT to binding.daySat,
                WeekDay.SUN to binding.daySun,
            )

            map.forEach { (day, view) ->
                view.isSelected = days.contains(day)
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.save.collect {
                    addMedicineViewModel.addNewMeds(requireContext().applicationContext)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.navigateAfterSave.collect {
                    findNavController().navigate(
                        R.id.action_resultFragment_to_medicinesFragment2,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph_meds, true)
                            .build()
                    )
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun isTakingTimes() {
        if (addMedicineViewModel.takingTimes.value.isNullOrEmpty()) {
            binding.layoutTakingTimes.visibility = View.GONE
            binding.layoutTakingTimes.setMarginTop(0)
        } else {
            binding.layoutTakingTimes.visibility = View.VISIBLE
            binding.layoutTakingTimes.setMarginTop(8)
        }
    }


    private fun setupEndTakingDate() {
        if (!addMedicineViewModel.selectedEndTakingDate.value.isNullOrBlank()) {
            binding.tvDateEnd.text = addMedicineViewModel.selectedEndTakingDate.value
            binding.tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.cvDateEnd.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cv_background_dark
                )
            )
            binding.tvDateEnd.setMarginStart(4)
        } else {
            binding.tvDateEnd.text = "Нет"
            binding.tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey
                )
            )
            binding.cvDateEnd.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cv_background_light
                )
            )
            binding.tvDateEnd.setMarginStart(0)
        }
    }
}