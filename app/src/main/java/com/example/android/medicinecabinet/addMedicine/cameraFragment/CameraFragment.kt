package com.example.android.medicinecabinet.addMedicine.cameraFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.databinding.FragmentCameraBinding
import com.example.android.medicinecabinet.utils.ProductUiState
import com.example.android.medicinecabinet.utils.TryAgainDialog
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class CameraFragment : Fragment() {
    lateinit var binding: FragmentCameraBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }
    private lateinit var barcodeScanner: BarcodeScanner

    private var cameraProvider: ProcessCameraProvider? = null
    private var isScanning = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CameraFragment", "onCreate loaded")
        addMedicineViewModel.setCode(null)
        addMedicineViewModel.resetCameraUiState()
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CameraFragment", "onViewCreated loaded")
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_CODE_128
            )
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)

        requestCameraPermission()


        addMedicineViewModel.uiStateCamera.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProductUiState.Loading -> {
                    showLoader()
                }
                is ProductUiState.Success -> {
                    findNavController().navigate(R.id.action_cameraFragment_to_nameFragment2)
                }

                is ProductUiState.Error -> {
                    hideLoader()
                    TryAgainDialog(startCamera = {
                        startCamera()
                    }).show(parentFragmentManager, "Try Arain")
                    addMedicineViewModel.setCode(null)

                }
                else -> Unit
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            isScanning = true

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(
                ContextCompat.getMainExecutor(requireContext())
            ) { imageProxy ->
                processImage(imageProxy)
            }

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue
                    if (!value.isNullOrEmpty()) {
                        onBarcodeDetected(value)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun onBarcodeDetected(code: String) {
        if (!isScanning) return
        isScanning = false

        cameraProvider?.unbindAll()

        addMedicineViewModel.loadProduct(code)
        addMedicineViewModel.setCode(code)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                showCameraPermissionDenied()
            }
        }

    fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun showCameraPermissionDenied() {
        val canAskAgain = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)

        if (canAskAgain) {
            Snackbar.make(
                requireView(),
                "Без доступа к камере невозможно сканировать штрих-код",
                Snackbar.LENGTH_LONG
            )
                .setAction("Разрешить") {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .show()
        } else {
            Snackbar.make(
                requireView(),
                "Доступ к камере отключён навсегда. Включите его в настройках",
                Snackbar.LENGTH_LONG
            )
                .setAction("Настройки") {
                    openAppSettings()
                }
                .show()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }


    private fun showLoader() {
        binding.progressBar.isVisible = true
        binding.progressBar.show()
    }

    private fun hideLoader(){
        binding.progressBar.isVisible = false
        binding.progressBar.hide()
    }

}