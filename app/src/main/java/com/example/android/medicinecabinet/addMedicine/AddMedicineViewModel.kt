package com.example.android.medicinecabinet.addMedicine


import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.addMedicine.changeSchedule.ChangeScheduleFragment.DateType
import com.example.android.medicinecabinet.utils.WeekDay
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.productInfo.ProductInfo
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.data.takingTime.TakingTimeUi
import com.example.android.medicinecabinet.utils.DateFormatter
import com.example.android.medicinecabinet.utils.ProductUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddMedicineViewModel(
    private val repository: MedicineRepository
) : ViewModel() {
    // CODE

    private var _code = MutableLiveData<String?>()
    val code: LiveData<String?> get() = _code

    fun setCode(code: String?) {
        _code.value = code
    }

    suspend fun fetchProductInfo(barcode: String): ProductInfo? =
        withContext(Dispatchers.IO) {
            val searchUrl = "https://felicia.md/ro/search?query=$barcode"

            val searchDoc = Jsoup
                .connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get()

            val productUrl = searchDoc
                .selectFirst("a[href*=/product/]")
                ?.absUrl("href")
                ?: return@withContext null

            val productDoc = Jsoup
                .connect(productUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get()

            val name = productDoc.selectFirst("h1.switcher-title")?.text()
            val priceLei = productDoc.selectFirst(".price__new-val")?.text()
            //val priceBan = productDoc.selectFirst("")?.text()

            if (name != null && priceLei != null) {
                ProductInfo(
                    code = code.value.toString(),
                    name = name,
                    priceLei = priceLei,
                    priceBan = null
                )
            } else {
                null
            }
        }

    /*var uiState by mutableStateOf<ProductUiState>(ProductUiState.Idle)
        private set*/

    private var _uiState = MutableLiveData<ProductUiState>(ProductUiState.Idle)
    val uiState: LiveData<ProductUiState>
        get() = _uiState

    fun resetUiState() {
        _uiState.value = ProductUiState.Idle
    }

    private val _product = MutableStateFlow<ProductInfo?>(null)
    val product: StateFlow<ProductInfo?> = _product

    fun loadProduct(barcode: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading

            try {
                _product.value = fetchProductInfo(barcode)

                if (_product.value != null){
                    _uiState.value = ProductUiState.Success(_product.value!!)
                } else {
                    _uiState.value = ProductUiState.Error("Такой товар не найден")
                }
            } catch (e: Exception){
                _uiState.value = ProductUiState.Error("Ошибка загрузки")
            }
        }
    }

    // VIEW MODEL FRAGMENT ADD 1 NAME

    private var _navToDosage = MutableLiveData<Boolean?>()
    val navToDosage: LiveData<Boolean?> get() = _navToDosage

    fun navNextToDosage() {
        _navToDosage.value = true
    }

    fun navNextToDosageDone() {
        _navToDosage.value = null
    }

    val textName = MutableLiveData<String>()
    val textQuantity = MutableLiveData<Int?>()
    val textDosage = MutableLiveData<Float?>()
    val selectedUnit = MutableLiveData<String?>()
    val textExpiration = MutableLiveData<String?>()


    private var _openDatePicker = MutableSharedFlow<Unit>()
    val openDatePicker = _openDatePicker.asSharedFlow()

    fun onOpenCalendarClicked() {
        viewModelScope.launch {
            _openDatePicker.emit(Unit)
        }
    }

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }


    // VIEW MODEL FRAGMENT ADD 2 DOSAGE

    val units = listOf("мг", "мкг", "г", "мл", "%")

    private var _onClickNext = MutableSharedFlow<Unit>()
    val onClickNext = _onClickNext.asSharedFlow()

    fun onNextClicked() {
        viewModelScope.launch {
            _onClickNext.emit(Unit)
        }
    }

    private var _onClickSkip = MutableSharedFlow<Unit>()
    val onClickSkip = _onClickSkip.asSharedFlow()

    fun onScipClicked() {
        viewModelScope.launch {
            _onClickSkip.emit(Unit)
        }
    }


    // VIEW MODEL FRAGMENT ADD 3 SCHEDULE

    val intakeInterval =
        listOf("По мере необходимости", "Каждый день", "В определённые дни", "Раз в несколько дней")

    private var _selectedIntakeInterval = MutableLiveData<String>("По мере необходимости")
    val selectedIntakeInterval: LiveData<String> get() = _selectedIntakeInterval

    fun setSelectedInterval(position: Int) {
        _selectedIntakeInterval.value = intakeInterval[position]
    }

    private val _daysInterval = MutableLiveData<Int?>(2)
    val daysInterval: LiveData<Int?> get() = _daysInterval

    fun setDaysInterval(days: Int?) {
        _daysInterval.value = days
    }

    fun formatInterval(days: Int?): String {
        return when (days) {
            2 -> "Через день"
            else -> "Каждые $days дней"
        }
    }

    val intervals = (2..100).toList()
    val displayIntervals: List<String> = intervals.map { formatInterval(it) }

    private var _navToResult = MutableSharedFlow<Unit>()
    val navToResult = _navToResult.asSharedFlow()

    fun navToResultClicked() {
        viewModelScope.launch {
            _navToResult.emit(Unit)
        }
    }

    private val _onClickAddTime = MutableSharedFlow<Unit>()
    val onClickAddTime = _onClickAddTime.asSharedFlow()

    fun onClickAddTimeDone() {
        viewModelScope.launch {
            _onClickAddTime.emit(Unit)
        }
    }

    private val _takingTimes = MutableLiveData<MutableList<TakingTimeUi>>(mutableListOf())
    val takingTimes: LiveData<MutableList<TakingTimeUi>> get() = _takingTimes

    fun addTakingTime(time: String) {
        val currentList = _takingTimes.value ?: mutableListOf()
        currentList.add(TakingTimeUi(time = time))
        _takingTimes.value = currentList.toMutableList()
    }

    fun deleteTakingTime(takingTime: TakingTimeUi) {
        val currentList = _takingTimes.value ?: mutableListOf()
        val position = currentList.indexOfFirst { it.id == takingTime.id }

        currentList.removeAt(position)
        _takingTimes.value = currentList
    }

    fun updateTakingTime(takingTime: TakingTimeUi, timeString: String) {
        val currentList = _takingTimes.value ?: mutableListOf()
        val position = currentList.indexOfFirst { it.id == takingTime.id }
        val newTime = TakingTimeUi(id = takingTime.id, time = timeString)

        if (position in currentList.indices) {
            currentList[position] = newTime
            _takingTimes.value = currentList
        }
    }

    fun clearTakingTimes() {
        _takingTimes.value = mutableListOf()
    }

    fun addTimesForMedicine(medsId: Long, takingTimes: List<TakingTimeUi>) {
        viewModelScope.launch(Dispatchers.IO) {
            val timesToInsert = takingTimes.map { time ->
                TakingTime(
                    medicineId = medsId.toInt(),
                    time = time.time
                )
            }
            repository.insertAllTimes(timesToInsert)
            Log.d("AddMedicineViewModel", "Times have been added - $timesToInsert")
            Log.d("AddMedicineViewModel", "Meds ID now $medsId")
        }
    }

    private var _changeSchedule = MutableSharedFlow<Unit>()
    val changeSchedule = _changeSchedule.asSharedFlow()

    fun onClickChangeSchedule() {
        viewModelScope.launch {
            _changeSchedule.emit(Unit)
        }
    }


    private val _selectedDays = MutableLiveData<MutableList<WeekDay>>(mutableListOf())
    val selectedDays: LiveData<MutableList<WeekDay>> get() = _selectedDays

    fun toggleDay(day: WeekDay) {
        val list = _selectedDays.value ?: mutableListOf()
        if (list.contains(day)) {
            if (list.size > 1) {
                list.remove(day)
            }
        } else list.add(day)
        _selectedDays.value = list.toMutableList()
    }

    fun initWithTodayIfEmpty() {
        if (_selectedDays.value.isNullOrEmpty()) {
            val today = LocalDate.now().dayOfWeek
            val weekDay = WeekDay.from(today)
            _selectedDays.value = mutableListOf(weekDay)
        }
    }

    fun clearSelectedDays() {
        _selectedDays.value = mutableListOf()
    }

    fun addDaysForMedicine(medsId: Long, weekDay: List<WeekDay>) {
        viewModelScope.launch(Dispatchers.IO) {
            val daysToInsert = weekDay.map { day ->
                SelectedTakingDays(
                    medicineId = medsId.toInt(),
                    weekDay = day
                )
            }
            repository.insertAllDays(daysToInsert)
        }
    }


    val isNextEnabled = MediatorLiveData<Boolean>().apply {
        fun update() {
            val times = takingTimes.value.orEmpty()
            val interval = selectedIntakeInterval.value

            value = if (interval == "По мере необходимости") {
                true
            } else {
                times.isNotEmpty()
            }
        }

        addSource(takingTimes) { update() }
        addSource(selectedIntakeInterval) { update() }
    }

    // VIEW MODEL FRAGMENT CHANGE SCHEDULE

    private var _navBackToSchedule = MutableSharedFlow<Unit>()
    val navBackToSchedule = _navBackToSchedule.asSharedFlow()

    fun navBackToScheduleDone() {
        viewModelScope.launch {
            _navBackToSchedule.emit(Unit)
        }
    }

    private var _onClickDateTaking = MutableSharedFlow<DateType>()
    val onClickDateTaking = _onClickDateTaking.asSharedFlow()

    fun onClickDateTakingStartDone() {
        viewModelScope.launch {
            _onClickDateTaking.emit(DateType.START)
        }
    }

    fun onClickDateTakingEndDone() {
        viewModelScope.launch {
            _onClickDateTaking.emit(DateType.END)
        }
    }

    private var _today = MutableLiveData<String>()

    fun setTodayDate(): String? {
        val todayDate = LocalDate.now()
        val pattern = DateFormatter.full(todayDate)

        val formatter = DateTimeFormatter.ofPattern(pattern, Locale("ru"))
        _today.value = todayDate.format(formatter)
        return _today.value
    }

    private var _selectedStartTakingDate = MutableLiveData<String?>()
    val selectedStartTakingDate: LiveData<String?> = _selectedStartTakingDate

    fun setSelectedStartTakingDate(date: String?) {
        _selectedStartTakingDate.value = date
    }

    private var _selectedEndTakingDate = MutableLiveData<String?>()
    val selectedEndTakingDate: LiveData<String?> get() = _selectedEndTakingDate

    fun setSelectedEndTakingDate(date: String?) {
        _selectedEndTakingDate.value = date
    }

    private var _deleteSelectedEndTakingDate = MutableSharedFlow<Unit>()
    val deleteSelectedEndTakingDate = _deleteSelectedEndTakingDate.asSharedFlow()

    fun deleteEndTakingDate() {
        viewModelScope.launch {
            _deleteSelectedEndTakingDate.emit(Unit)
            _selectedEndTakingDate.value = null
        }
    }


    // VIEW MODEL FRAGMENT ADD 4 RESULT


    private var _navigateAfterSave = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigateAfterSave = _navigateAfterSave.asSharedFlow()

    fun onMedsClicked() {
        addNewMeds()
    }

    fun addNewMeds() {
        when (selectedIntakeInterval.value) {
            "По мере необходимости" -> {
                setSelectedStartTakingDate(null)
                setSelectedEndTakingDate(null)
                clearTakingTimes()
                clearSelectedDays()
                setDaysInterval(null)
            }

            "Каждый день" -> {
                clearSelectedDays()
                setDaysInterval(null)
            }

            "В определённые дни" -> setDaysInterval(null)
            "Раз в несколько дней" -> clearSelectedDays()
        }


        viewModelScope.launch(Dispatchers.IO) {
            val newMedicine = Medicine.MedicineBuilder()
                .name(textName.value ?: "NULL")
                .image(null)
                .quantity(textQuantity.value)
                .expirationDate(textExpiration.value)
                .dosage(textDosage.value)
                .unit(selectedUnit.value)
                .startTakingDate(_selectedStartTakingDate.value)
                .endTakingDate(_selectedEndTakingDate.value)
                .intakeIntervalDays(daysInterval.value)
                .code(code.value)
                .build()

            val medsId = repository.insert(newMedicine)
            Log.d("AddMedicineViewModel", "Meds ID now $medsId")
            Log.d("AddMedicineViewModel", "Medicine has added $newMedicine")

            val times = takingTimes.value ?: emptyList()

            addTimesForMedicine(medsId, times)

            Log.d("AddMedicineViewModel", "Times for medicine $times")

            val days = selectedDays.value ?: emptyList()

            addDaysForMedicine(medsId, days)

            _navigateAfterSave.emit(Unit)
        }

    }

}