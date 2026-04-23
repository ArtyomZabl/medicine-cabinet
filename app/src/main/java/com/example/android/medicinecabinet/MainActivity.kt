package com.example.android.medicinecabinet

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.SessionConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModelFactory
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.databinding.ActivityMainBinding
import com.example.android.medicinecabinet.databinding.FragmentCameraBinding
import com.example.android.medicinecabinet.utils.Constance
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding

    private lateinit var repository: MedicineRepository
    lateinit var factory: AddMedicineViewModelFactory
    val addMedicineViewModel: AddMedicineViewModel by lazy {
        ViewModelProvider(this, factory)[AddMedicineViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel(this)
        checkNotificationPermission()

        val daoMeds = MedicineDatabase.getDatabase(this).medicineDao()
        val daoTime = MedicineDatabase.getDatabase(this).takingTimeDao()
        val daoDays = MedicineDatabase.getDatabase(this).selectedTakingDaysDao()
        val daoLog = MedicineDatabase.getDatabase(this).medicineLogDao()
        repository = MedicineRepository(daoMeds, daoTime, daoDays, daoLog)
        factory = AddMedicineViewModelFactory(repository = repository, application = application)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.mainBottomNavigation)
        bottomNavigationView.setupWithNavController(navController)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val container = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBarInsets.top,
                left = systemBarInsets.left,
                right = systemBarInsets.right
            )
            insets
        }
    }

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            Constance.MEDICINE_CHANNEL_ID,
            "Напоминание о лекарствах",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
        }

        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }
}

