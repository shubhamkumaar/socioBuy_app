package com.example.buynow.presentation.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call

import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.buynow.presentation.adapter.CartAdapter
import com.example.buynow.presentation.adapter.CartItemClickAdapter

import com.example.buynow.R


import com.example.buynow.data.local.room.CartViewModel
import com.example.buynow.data.local.room.ProductEntity
import com.example.buynow.data.model.AiRequest
import com.example.buynow.data.model.AiResponse
import com.example.buynow.presentation.activity.RetrofitInstance

class BagFragment : Fragment(), CartItemClickAdapter {

    lateinit var cartRecView:RecyclerView
    lateinit var cartAdapter: CartAdapter
    lateinit var animationView: LottieAnimationView
    lateinit var totalPriceBagFrag:TextView
    lateinit var Item: ArrayList<ProductEntity>
     var sum:Int = 0

    private lateinit var cartViewModel: CartViewModel
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_bag, container, false)

        cartRecView = view.findViewById(R.id.cartRecView)
        animationView = view.findViewById(R.id.animationViewCartPage)
        totalPriceBagFrag = view.findViewById(R.id.totalPriceBagFrag)
        val bottomCartLayout:LinearLayout = view.findViewById(R.id.bottomCartLayout)
        val emptyBagMsgLayout:LinearLayout = view.findViewById(R.id.emptyBagMsgLayout)
        val MybagText:TextView = view.findViewById(R.id.MybagText)
        val aiSuggestion:Button = view.findViewById<Button>(R.id.ai_suggestion_button)
        val checkoutBtn:Button = view.findViewById<Button>(R.id.checkOut_BagPage)
        Item = arrayListOf()


        animationView.playAnimation()
        animationView.loop(true)
        bottomCartLayout.visibility = View.GONE
        MybagText.visibility = View.GONE
        emptyBagMsgLayout.visibility = View.VISIBLE


        cartRecView.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(activity as Context, this )
        cartRecView.adapter = cartAdapter


        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)

        cartViewModel.allproducts.observe(viewLifecycleOwner, Observer {List ->
            List?.let {
                cartAdapter.updateList(it)
                Item.clear()
                sum = 0
                Item.addAll(it)
            }

            if (List.size == 0){
                animationView.playAnimation()
                animationView.loop(true)
                bottomCartLayout.visibility = View.GONE
                MybagText.visibility = View.GONE
                aiSuggestion.visibility=View.GONE
                emptyBagMsgLayout.visibility = View.VISIBLE

            }
            else{
                emptyBagMsgLayout.visibility = View.GONE
                bottomCartLayout.visibility = View.VISIBLE
                aiSuggestion.visibility=View.VISIBLE
                MybagText.visibility = View.VISIBLE
                animationView.pauseAnimation()
            }

            Item.forEach {
                sum += it.price
            }
            totalPriceBagFrag.text = "$" + sum
        })

        aiSuggestion.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val authToken = sharedPref.getString("auth_token", null)

            if (authToken == null) {
                Toast.makeText(context, "Missing Auth Token!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val productIds = if (Item.isNotEmpty()) {
                Item.map { it.pId }
            } else {
                listOf(39764445) // Default fallback product
            }

            Log.d("AI_REQUEST", "Sending productIds: $productIds")

            val request = AiRequest(productId = productIds)

            RetrofitInstance.apiInterface.getAiRecommendation("Bearer $authToken", request)
                .enqueue(object : retrofit2.Callback<AiResponse> {
                    override fun onResponse(call: Call<AiResponse>, response: retrofit2.Response<AiResponse>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.message ?: "[]"
                            Log.d("AI_RESPONSE_RAW", responseBody)

                            try {
                                val jsonArray = org.json.JSONArray(responseBody)
                                if (jsonArray.length() > 0) {
                                    val obj = jsonArray.getJSONObject(0)
                                    val productName = obj.optString("productName", "Unknown Product")
                                    val message = obj.optString("message", "No message available.")
                                    showAiDialog(productName, message)
                                } else {
                                    showAiDialog("No Match", "No AI recommendations were found.")
                                }
                            } catch (e: Exception) {
                                Log.e("AI_PARSE_ERROR", "Failed to parse AI response", e)
                                Toast.makeText(context, "Parsing error in AI response", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "AI API failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                        Toast.makeText(context, "AI API Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        Log.e("AI_FAILURE", "Exception", t)
                    }
                })
        }


        checkoutBtn.setOnClickListener{
            fun startOrderPlacedDialog() {
                val builder = AlertDialog.Builder(activity)
//                val inflater = activity.layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_order_placed, null)
                builder.setView(dialogView)
                builder.setCancelable(false) // User must click OK to dismiss

                dialog = builder.create()

                // Set up the OK button click listener
                val okButton: Button = dialogView.findViewById(R.id.orderPlacedOkButton)
                okButton.setOnClickListener {
                    dismissDialog()
                }

                dialog?.show()
            }
            startOrderPlacedDialog()
        }
        return view
    }
    private fun showAiDialog(productName: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("🧠 AI Suggestion")
        builder.setMessage("✨ Product: $productName\n\n💬 Suggestion:\n$message")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }
    override fun onItemDeleteClick(product: ProductEntity) {
        cartViewModel.deleteCart(product)
        Toast.makeText(context,"Removed From Bag",Toast.LENGTH_SHORT).show()
    }

    override fun onItemUpdateClick(product: ProductEntity) {
        cartViewModel.updateCart(product)
    }


}