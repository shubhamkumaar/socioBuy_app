package com.example.buynow.repositry

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.buynow.data.model.Product
import com.example.buynow.data.model.ProductResponse
import com.example.buynow.presentation.activity.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductRepository(private val context: Context) {

    val productData = MutableLiveData<Pair<Map<String, List<Product>>, List<Product>>>()

    fun fetchProducts() {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) {
            Log.e("ProductRepository", "Token is null, cannot make API call")
            return
        }

        val bearerToken = "Bearer $token"
        Log.d("API_CALL", "Calling API to fetch products...")
        RetrofitInstance.apiInterface.getProducts(bearerToken)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val categories = body?.categories ?: emptyMap()
                        val cover = body?.cover_products ?: emptyList()
                        productData.value = Pair(categories, cover)
                        Log.d("ProductRepository", "Product fetch successful.")
                        Log.d("API_DATA", "Raw Response: ${response.body()}")

                    } else {
                        Log.e("ProductRepository", "Error: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Log.e("ProductRepository", "API call failed: ${t.localizedMessage}")
                }
            })
    }
}
