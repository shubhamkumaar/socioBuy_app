package com.example.buynow.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.buynow.repositry.ProductRepository

// class ProductViewModel(application: Application) : AndroidViewModel(application) {
//    private val repository = ProductRepository(application.applicationContext)
//    val products: LiveData<Map<String, List<Product>>> = repository.productData
//
//    fun loadProducts() {
//        repository.fetchProducts()
//    }}

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(application.applicationContext)
    val productResult = repository.productData

    fun loadProducts() {
        repository.fetchProducts()
    }
}

// class ProductViewModel(context: Context) : ViewModel() {
//    private val repository = ProductRepository(context)
//    val products: LiveData<Map<String, List<Product>>> = repository.productData
//
//    fun loadProducts() {
//        repository.fetchProducts()
//    }
// }
