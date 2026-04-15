package com.example.android.medicinecabinet.addMedicine


import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.android.medicinecabinet.addMedicine.changeSchedule.ChangeScheduleFragment.DateType
import com.example.android.medicinecabinet.utils.WeekDay
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.productInfo.ProductInfo
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.data.takingTime.TakingTimeUi
import com.example.android.medicinecabinet.utils.Alarm
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
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddMedicineViewModel(
    private val repository: MedicineRepository,
    application: Application
) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext


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
            val description = productDoc.selectFirst(".description-container")?.text()
            //val imageUrl = "https://felicia.md${productDoc.selectFirst(".detail-gallery-big__picture")?.text()}"
            val imageUrl = productDoc
                .selectFirst("a[href*=/upload/iblock/]")
                ?.absUrl("href")

            if (name != null) {
                ProductInfo(
                    code = code.value.toString(),
                    name = name,
                    priceLei = priceLei,
                    imageUrl = imageUrl,
                    description = description
                )
            } else {
                null
            }
        }

    private var _uiStateCamera = MutableLiveData<ProductUiState>(ProductUiState.Idle)
    val uiStateCamera: LiveData<ProductUiState> get() = _uiStateCamera

    fun resetCameraUiState() {
        _uiStateCamera.value = ProductUiState.Idle
    }

    fun changeCameraUiState(uiState: ProductUiState) {
        _uiStateCamera.value = uiState
    }

    private val _product = MutableStateFlow<ProductInfo?>(null)
    val product: StateFlow<ProductInfo?> = _product

    fun setProductToNull() {
        _product.value = null
    }

    fun changeProductName(name: String?) {
        _product.value?.name = name
    }

    fun loadProduct(barcode: String) {
        viewModelScope.launch {
            _uiStateCamera.value = ProductUiState.Loading

            try {
                _product.value = fetchProductInfo(barcode)


                if (_product.value != null) {
                    _uiStateCamera.value = ProductUiState.Success(_product.value!!)
                } else {
                    _uiStateCamera.value = ProductUiState.Error("Такой товар не найден")
                }
            } catch (e: Exception) {
                _uiStateCamera.value = ProductUiState.Error("Ошибка загрузки")
            }
        }
    }

    private var _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?> get() = _imagePath

    suspend fun downloadImage(url: String) {
        val loader = ImageLoader(appContext)
        val request = ImageRequest.Builder(appContext)
            .data(url)
            .allowHardware(false)
            .build()

        val result = (loader.execute(request).drawable as BitmapDrawable).bitmap

        val file = File(appContext.cacheDir, "med_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            result.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        _imagePath.postValue(file.absolutePath)
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
    val textQuantity = MutableLiveData<String?>()
    val textDosage = MutableLiveData<String?>()
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


    val isNameNextEnabled = MediatorLiveData<Boolean>().apply {
        addSource(textName) { value = !it.isNullOrBlank() && !textQuantity.value.isNullOrBlank() }
        addSource(textQuantity) { value = !it.isNullOrBlank() && !textName.value.isNullOrBlank() }
    }

    // VIEW MODEL FRAGMENT ADD 2 DOSAGE

    val units = listOf("мг", "мкг", "г", "мл", "%")

    val isDosageNextEnabled = MediatorLiveData<Boolean>().apply {
        addSource(textDosage) { value = !it.isNullOrBlank() && !selectedUnit.value.isNullOrBlank() }
        addSource(selectedUnit) { value = !textDosage.value.isNullOrBlank() && !it.isNullOrBlank() }

    }


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


    suspend fun addTimesForMedicine(medsId: Long, takingTimes: List<TakingTimeUi>): List<TakingTime> =
        withContext(Dispatchers.IO) {
            val timesToInsert = takingTimes.map { time ->
                TakingTime(
                    medicineId = medsId.toInt(),
                    time = time.time
                )
            }
            val ids = repository.insertAllTimes(timesToInsert)
            val result = timesToInsert.mapIndexed { index, takingTime ->
                takingTime.copy(id = ids[index].toInt())
            }
            Log.d("AddMedicineViewModel", "Times have been added with IDs: $result")
            result
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

    suspend fun addDaysForMedicine(medsId: Long, weekDay: List<WeekDay>) =
        withContext(Dispatchers.IO) {
            val daysToInsert = weekDay.map { day ->
                SelectedTakingDays(
                    medicineId = medsId.toInt(),
                    weekDay = day
                )
            }
            repository.insertAllDays(daysToInsert)
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

    private val _save = MutableSharedFlow<Unit>()
    val save = _save.asSharedFlow()

    fun onMedsClicked() {
        viewModelScope.launch {
            _save.emit(Unit)
        }
    }

    fun addNewMeds(context: Context) {
        viewModelScope.launch {
            product.value?.imageUrl?.let { url ->
                try {
                    downloadImage(url)
                } catch (e: Exception) {
                    Log.e("ERROR", "Ошибка загрузки фото: ${e.message}")
                }
            }

            prepareDataBasedOnInterval()

            withContext(Dispatchers.IO) {
                val newMedicine = Medicine.MedicineBuilder()
                    .name(textName.value ?: "NULL")
                    .image(_imagePath.value)
                    .quantity(textQuantity.value?.toInt())
                    .expirationDate(textExpiration.value)
                    .dosage(textDosage.value?.toFloat())
                    .unit(selectedUnit.value)
                    .startTakingDate(_selectedStartTakingDate.value)
                    .endTakingDate(_selectedEndTakingDate.value)
                    .intakeIntervalDays(daysInterval.value)
                    .code(code.value)
                    .description(_product.value?.description)
                    .build()

                // 1. Сохраняем лекарство и получаем его реальный ID
                val medsId = repository.insert(newMedicine)
                val medicineWithId = newMedicine.copy(medicineId = medsId.toInt())

                Log.d("AddMedicineViewModel", "Meds ID now $medsId")

                // 2. Сохраняем время приема и получаем список с реальными ID
                val uiTimes = takingTimes.value ?: emptyList()
                val timesWithIds = addTimesForMedicine(medsId, uiTimes)

                // 3. Сохраняем дни
                val days = selectedDays.value ?: emptyList()
                addDaysForMedicine(medsId, days)

                // 4. Планируем будильник, используя ID
                Alarm.scheduleAlarm(context, medicineWithId, timesWithIds)

                _navigateAfterSave.emit(Unit)
            }
        }
    }

    fun prepareDataBasedOnInterval() {
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
    }

    fun resetAddMedicineState() {
        // Сброс информации о продукте (от сканера)
        _code.value = null
        resetCameraUiState() // Устанавливает _uiState в ProductUiState.Idle
        setProductToNull() // Устанавливает _product в null
        _imagePath.value = null

        // Сброс полей с первого экрана (Название и количество)
        textName.value = "" // или null, если допускается
        textQuantity.value = null
        textExpiration.value = null
        _selectedDate.value = null

        // Сброс полей со второго экрана (Дозировка)
        textDosage.value = null
        selectedUnit.value = null

        // Сброс полей с третьего экрана (Расписание)
        _selectedIntakeInterval.value = intakeInterval.first() // "По мере необходимости"
        _daysInterval.value = 2 // Значение по умолчанию
        clearTakingTimes() // Очищает список времени приема
        clearSelectedDays() // Очищает список выбранных дней

        // Сброс полей с экрана изменения расписания
        _selectedStartTakingDate.value = null
        _selectedEndTakingDate.value = null
    }

}