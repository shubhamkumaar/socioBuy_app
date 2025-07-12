package com.example.buynow.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buynow.R
import com.example.buynow.data.local.room.ProductEntity

class CartAdapter(private val ctx: Context, val listener: CartItemClickAdapter) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val cartList: ArrayList<ProductEntity> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val cartView = LayoutInflater.from(ctx).inflate(R.layout.cart_item_single, parent, false)

        return CartViewHolder(cartView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem: ProductEntity = cartList[position]

        holder.cartName.text = cartItem.name
        val exchangeRate = 83.25 // 1 USD = 83.25 INR
        val priceInINR = (cartItem.price * exchangeRate * cartItem.qua).toInt()
        holder.cartPrice.text = "₹%,d".format(priceInINR)


        holder.quantityTvCart.text = cartItem.qua.toString()
        holder.cartMore.setOnClickListener {
        }

        Glide.with(ctx)
            .load(cartItem.Image)
            .into(holder.cartImage)


        holder.btnIncrease.setOnClickListener {
            cartItem.qua += 1
            holder.quantityTvCart.text = cartItem.qua.toString()
            val newPrice = (cartItem.price * exchangeRate * cartItem.qua).toInt()
            holder.cartPrice.text = "₹$newPrice"
            listener.onItemUpdateClick(cartItem)
            listener.onCartChanged(cartList)
        }
        holder.btnDecrease.setOnClickListener {
            if (cartItem.qua > 1) {
                cartItem.qua -= 1
                holder.quantityTvCart.text = cartItem.qua.toString()
                val newPrice = (cartItem.price * exchangeRate * cartItem.qua).toInt()
                holder.cartPrice.text = "₹$newPrice"
                listener.onItemUpdateClick(cartItem)
            } else {
                listener.onItemDeleteClick(cartItem)
            }
            listener.onCartChanged(cartList)
        }
        holder.cartMore.setOnClickListener {
            listener.onItemDeleteClick(cartItem)
            cartList.removeAt(position)
            notifyItemRemoved(position)
            listener.onCartChanged(cartList)
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cartImage: ImageView = itemView.findViewById(R.id.cartImage)
        val cartMore: ImageView = itemView.findViewById(R.id.cartMore)
        val cartName: TextView = itemView.findViewById(R.id.cartName)
        val cartPrice: TextView = itemView.findViewById(R.id.cartPrice)
        val quantityTvCart: TextView = itemView.findViewById(R.id.quantityTvCart)
        val btnIncrease: View = itemView.findViewById(R.id.plusLayout)
        val btnDecrease: View = itemView.findViewById(R.id.minusLayout)
    }

    fun updateList(newList: List<ProductEntity>) {
        cartList.clear()
        cartList.addAll(newList)
        notifyDataSetChanged()
    }
}

interface CartItemClickAdapter {
    fun onItemDeleteClick(product: ProductEntity)
    fun onItemUpdateClick(product: ProductEntity)
    fun onCartChanged(cartList: List<ProductEntity>)
}
