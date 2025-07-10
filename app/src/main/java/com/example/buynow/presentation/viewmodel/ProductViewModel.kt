package com.example.buynow.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.buynow.data.model.Product
import com.example.buynow.repositry.ProductRepository

//class ProductViewModel(application: Application) : AndroidViewModel(application) {
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

//class ProductViewModel(context: Context) : ViewModel() {
//    private val repository = ProductRepository(context)
//    val products: LiveData<Map<String, List<Product>>> = repository.productData
//
//    fun loadProducts() {
//        repository.fetchProducts()
//    }
//}


