package com.example.android.medicinecabinet.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.takingTime.TakingTimeAdapter
import com.example.android.medicinecabinet.data.takingTime.TakingTimeUi
import com.example.android.medicinecabinet.databinding.FragmentDetailBinding
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.utils.DateFormatter
import com.example.android.medicinecabinet.utils.DeleteDialogFragment
import com.example.android.medicinecabinet.utils.Functions.setMarginTop
import com.example.android.medicinecabinet.utils.WeekDay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class DetailFragment : Fragment(R.layout.fragment_detail) {

    lateinit var binding: FragmentDetailBinding
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TakingTimeAdapter()
        val repository =
            MedicineRepository(
                MedicineDatabase.getDatabase(requireContext()).medicineDao(),
                MedicineDatabase.getDatabase(requireContext()).takingTimeDao(),
                MedicineDatabase.getDatabase(requireContext()).selectedTakingDaysDao(),
                MedicineDatabase.getDatabase(requireContext()).medicineLogDao()
            )
        val factory = DetailViewModelFactory(repository)
        val detailViewModel = ViewModelProvider(this, factory)[DetailViewModel::class.java]

        binding.detailViewModel = detailViewModel
        binding.lifecycleOwner = this

        binding.rcTakingTimes.adapter = adapter
        binding.rcTakingTimes.layoutManager = LinearLayoutManager(context)


        val medicineId = args.medicineId
        detailViewModel.loadMedsById(medicineId)
        detailViewModel.loadTimesAndDaysForMeds(medicineId)


        detailViewModel.medicine.observe(viewLifecycleOwner, Observer { medicine ->
            binding.apply {
                medName.text = medicine.name
                medQuantity.text = medicine.quantity.toString()
                code.text = medicine.code

                if (!medicine.imagePath.isNullOrEmpty()) {
                    medImage.load(File(medicine.imagePath)) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder)
                    }
                    medImage.visibility = View.VISIBLE

                } else medImage.visibility = View.GONE

                if(medicine.expirationDate.isNullOrBlank()) {
                    divider?.visibility = View.GONE
                    divider?.setMarginTop(0)

                    expiration?.visibility = View.GONE

                    textExpiration?.visibility = View.GONE
                    textExpiration?.setMarginTop(0)
                } else {
                    val todayDate = LocalDate.now()
                    val expDate = LocalDate.parse(medicine.expirationDate)

                    textExpiration?.text = DateFormatter.fullUi(expDate)

                    when {
                        expDate.isBefore(todayDate) -> {
                            textExpiration?.setTextColor(textExpiration.context.getColor(R.color.red))
                            textExpiration?.setTypeface(null, android.graphics.Typeface.BOLD)
                        }
                        expDate.isBefore(todayDate.plusDays(7)) -> {
                            textExpiration?.setTextColor(textExpiration.context.getColor(R.color.orange))
                            textExpiration?.setTypeface(null, android.graphics.Typeface.NORMAL)
                        }
                        else -> {
                            textExpiration?.setTextColor(textExpiration.context.getColor(R.color.black))
                            textExpiration?.setTypeface(null, android.graphics.Typeface.NORMAL)
                        }
                    }

                    divider?.visibility = View.VISIBLE
                    divider?.setMarginTop(8)

                    expiration?.visibility = View.VISIBLE
                    expiration?.setMarginTop(8)

                    textExpiration?.visibility = View.VISIBLE
                }


                if (medicine.startTakingDate != null) {
                    tvDateStart.text = medicine.startTakingDate
                    isTakingDate(medicine)
                    constraintLayoutDuration.visibility = View.VISIBLE
                } else constraintLayoutDuration.visibility = View.GONE

                if (medicine.intakeIntervalDays == null) {
                    tvIntakeDays.visibility = View.GONE
                    tvIntakeDays.setMarginTop(0)
                } else {
                    val days = medicine.intakeIntervalDays
                    tvIntakeDays.text = when (days) {
                        2 -> "Через день"
                        else -> "Каждые $days дней"
                    }
                    tvIntakeDays.visibility = View.VISIBLE
                    tvIntakeDays.setMarginTop(8)
                }
            }
        })



        detailViewModel.allTimesThisMeds.observe(viewLifecycleOwner) { allTimes ->
            if (allTimes.isNullOrEmpty()) {
                binding.constraintLayoutSchedule.visibility = View.GONE
                binding.constraintLayoutSchedule.setMarginTop(0)

                binding.cvTakingTimes.visibility = View.GONE
                binding.cvTakingTimes.setMarginTop(0)

                binding.constraintLayoutDuration.visibility = View.GONE
                binding.constraintLayoutDuration.setMarginTop(0)
            } else {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE
                binding.constraintLayoutSchedule.setMarginTop(16)

                binding.cvTakingTimes.visibility = View.VISIBLE
                binding.cvTakingTimes.setMarginTop(16)

                binding.constraintLayoutDuration.visibility = View.VISIBLE
                binding.constraintLayoutDuration.setMarginTop(16)
            }

            allTimes?.let { times ->
                val item = times.map { takingTime ->
                    TakingTimeUi(
                        id = takingTime.id,
                        time = takingTime.time
                    )
                }
                adapter.submitListByType(item, Constance.ITEM_TYPE_TIME_TAKING_VIEW)
            }
        }

        detailViewModel.getSelectedDaysForMedicine().observe(viewLifecycleOwner) { days ->
            if (days.isNullOrEmpty() && detailViewModel.medicine.value?.intakeIntervalDays == null) {
                binding.constraintLayoutSchedule.visibility = View.GONE
                binding.constraintLayoutSchedule.setMarginTop(0)
            } else if (days.isNullOrEmpty()) {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE
                binding.constraintLayoutSchedule.setMarginTop(16)

                binding.layoutWeekDays.visibility = View.GONE
                binding.layoutWeekDays.setMarginTop(0)
            } else {
                binding.constraintLayoutSchedule.visibility = View.VISIBLE
                binding.constraintLayoutSchedule.setMarginTop(16)

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
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailViewModel.onClickBack.collect {
                    findNavController().navigateUp()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailViewModel.onClickDelete.collect {
                    DeleteDialogFragment {
                        detailViewModel.delete(detailViewModel.medicine.value!!)
                    }.show(parentFragmentManager, "deleteDialog")
                }
            }
        }
    }


    fun FragmentDetailBinding.isTakingDate(medicine: Medicine?) {
        if (medicine?.endTakingDate == null) {
            tvDateEnd.text = "Нет"
            tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_grey
                )
            )
        } else {
            tvDateEnd.text = medicine.endTakingDate
            tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
    }
}


