package com.example.android.medicinecabinet

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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

        val daoMeds = MedicineDatabase.getDatabase(this).medicineDao()
        val daoTime = MedicineDatabase.getDatabase(this).takingTimeDao()
        val daoDays = MedicineDatabase.getDatabase(this).selectedTakingDaysDao()
        repository = MedicineRepository(daoMeds, daoTime, daoDays)
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
}