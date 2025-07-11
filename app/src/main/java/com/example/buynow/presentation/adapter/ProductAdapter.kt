package com.example.buynow.presentation.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buynow.R
import com.example.buynow.data.model.Product
import com.example.buynow.presentation.activity.ProductDetailsActivity

class ProductAdapter(private val products: List<Product>, private val context: Context,private val productFrom: String) :

    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage_singleProduct)
        val productName: TextView = itemView.findViewById(R.id.productName_singleProduct)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice_singleProduct)
        val productContainer: LinearLayout = itemView.findViewById(R.id.productContainer_single)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        var TAG = "ADAPTER"
        val product = products[position]
        Log.d(TAG,product.productName)
        holder.productName.text = product.productName
        holder.productPrice.text = "₹${product.productPrice}"

        Glide.with(context)
            .load(product.productImage)
            //.placeholder(R.drawable)
            .into(holder.productImage)

        // Reusable click logic
        val openDetails = View.OnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("ProductIndex", product.productId) // Pass actual ID
            intent.putExtra("ProductFrom", productFrom) // "Category", "Cover", etc.
            context.startActivity(intent)
        }

        // ✅ Make both image and container clickable
        holder.productImage.setOnClickListener(openDetails)
        holder.productContainer.setOnClickListener(openDetails)
    }
}
