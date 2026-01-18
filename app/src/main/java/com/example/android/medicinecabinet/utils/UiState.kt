package com.example.android.medicinecabinet.utils

import com.example.android.medicinecabinet.data.productInfo.ProductInfo

sealed interface ProductUiState {
    object Idle: ProductUiState
    object Loading: ProductUiState
    data class Success(val product: ProductInfo) : ProductUiState
    data class Error(val message: String) : ProductUiState
}