package com.example.buynow.presentation.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.buynow.R
import com.example.buynow.data.local.room.CartViewModel
import com.example.buynow.data.local.room.ProductEntity
import com.example.buynow.data.model.AiRequest
import com.example.buynow.data.model.AiResponse
import com.example.buynow.presentation.LoadingDialog
import com.example.buynow.presentation.activity.RetrofitInstance
import com.example.buynow.presentation.adapter.CartAdapter
import com.example.buynow.presentation.adapter.CartItemClickAdapter
import org.json.JSONArray
import retrofit2.Call

class BagFragment : Fragment(), CartItemClickAdapter {
    lateinit var loadingDialog: LoadingDialog

    lateinit var cartRecView: RecyclerView
    lateinit var cartAdapter: CartAdapter
    lateinit var animationView: LottieAnimationView
    lateinit var totalPriceBagFrag: TextView
    lateinit var Item: ArrayList<ProductEntity>
    var sum: Double = 0.0

    private lateinit var cartViewModel: CartViewModel
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bag, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        cartRecView = view.findViewById(R.id.cartRecView)
        animationView = view.findViewById(R.id.animationViewCartPage)
        totalPriceBagFrag = view.findViewById(R.id.totalPriceBagFrag)
        val bottomCartLayout: LinearLayout = view.findViewById(R.id.bottomCartLayout)
        val emptyBagMsgLayout: LinearLayout = view.findViewById(R.id.emptyBagMsgLayout)
        val MybagText: TextView = view.findViewById(R.id.MybagText)
        val aiSuggestion: Button = view.findViewById<Button>(R.id.ai_suggestion_button)
        val checkoutBtn: Button = view.findViewById<Button>(R.id.checkOut_BagPage)
        Item = arrayListOf()
        val sparkleAnimation = view.findViewById<LottieAnimationView>(R.id.lottie_sparkle)

        animationView.playAnimation()
        animationView.loop(true)
        bottomCartLayout.visibility = View.GONE
        MybagText.visibility = View.GONE
        emptyBagMsgLayout.visibility = View.VISIBLE

        cartRecView.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(activity as Context, this)
        cartRecView.adapter = cartAdapter

        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)

        cartViewModel.allproducts.observe(
            viewLifecycleOwner,
            Observer { List ->
                List?.let {
                    cartAdapter.updateList(it)
                    Item.clear()
                    sum = 0.0
                    Item.addAll(it)
                }

                if (List.size == 0) {
                    animationView.playAnimation()
                    animationView.loop(true)
                    bottomCartLayout.visibility = View.GONE
                    MybagText.visibility = View.GONE
                    aiSuggestion.visibility = View.GONE
                    emptyBagMsgLayout.visibility = View.VISIBLE
                } else {
                    emptyBagMsgLayout.visibility = View.GONE
                    bottomCartLayout.visibility = View.VISIBLE
                    aiSuggestion.visibility = View.VISIBLE
                    MybagText.visibility = View.VISIBLE
                    animationView.pauseAnimation()
                }

            sum = 0.0
            Item.forEach {
                sum += it.price * it.qua
            }
            totalPriceBagFrag.text = "₹${String.format("%.2f", sum)}"
        })

        aiSuggestion.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val authToken = sharedPref.getString("auth_token", null)

            if (authToken == null) {
                if (isAdded) {
                    context?.let {
                        Toast.makeText(it, "Missing Auth Token!", Toast.LENGTH_SHORT).show()
                    }
                }
                return@setOnClickListener
            }

            val productIds = if (Item.isNotEmpty()) {
                Item.map { it.pId }
            } else {
                listOf(39764445) // Fallback product
            }
            sparkleAnimation.visibility = View.VISIBLE
            sparkleAnimation.playAnimation()
            // loadingDialog?.startLoadingDialog()

            Log.d("AI_REQUEST", "Sending productIds: $productIds")

            val request = AiRequest(productId = productIds)

            RetrofitInstance.apiInterface.getAiRecommendation("Bearer $authToken", request)
                .enqueue(object : retrofit2.Callback<AiResponse> {
                    override fun onResponse(call: Call<AiResponse>, response: retrofit2.Response<AiResponse>) {
                        // loadingDialog?.dismissDialog()
                        sparkleAnimation.pauseAnimation()
                        sparkleAnimation.visibility = View.GONE
                        if (!isAdded) return

                        if (response.isSuccessful) {
                            val responseBody = response.body()?.message ?: "[]"
                            Log.d("AI_RESPONSE_RAW", responseBody)

//                            try {
//
//                                val jsonArray = JSONArray(responseBody)
//                                if (jsonArray.length() > 0) {
//                                    val obj = jsonArray.getJSONObject(0)
//                                    val productName = obj.optString("productName", "Unknown Product")
//                                    val message = obj.optString("message", "No message available.")
//                                    showAiDialog(productName, message)
//                                } else {
//                                    showAiDialog("No Match", "No AI recommendations were found.")
//                                }
//                            }
                            try {
                                val jsonArray = org.json.JSONArray(responseBody)
                                if (jsonArray.length() > 0) {
                                    // Loop through every recommendation
                                    val suggestions = mutableListOf<Pair<String, String>>()

                                    for (i in 0 until jsonArray.length()) {
                                        val obj = jsonArray.getJSONObject(i)
                                        val productName = obj.optString("productName", "Unknown Product")
                                        val message = obj.optString("message", "No message available.")

                                        // Show dialog with delay for next
//                                        Handler(Looper.getMainLooper()).postDelayed({
//                                            showAiDialog(productName, message)
//                                        }, i * 500L) // 500ms delay between dialogs
                                        suggestions.add(Pair(productName, message))
                                    }
                                    showAiDialog(suggestions)
                                } else {
                                    showAiDialog(listOf(Pair("No Match", "No AI recommendations were found.")))
                                }
                            } catch (e: Exception) {
                                Log.e("AI_PARSE_ERROR", "Failed to parse AI response", e)
                                if (isAdded) {
                                    context?.let {
                                        Toast.makeText(it, "Parsing error in AI response", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            if (isAdded) {
                                context?.let {
                                    Toast.makeText(it, "AI API failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                        Log.e("AI_FAILURE", "Exception", t)
                        // loadingDialog?.dismissDialog()
                        sparkleAnimation.pauseAnimation()
                        sparkleAnimation.visibility = View.GONE
                        if (isAdded) {
                            context?.let {
                                Toast.makeText(it, "AI API Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
        }

        checkoutBtn.setOnClickListener {
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
    private fun getShortProductName(fullName: String): String {
        // Remove all non-letter characters and split
        val words = fullName
            .replace("[^A-Za-z ]".toRegex(), "") // remove punctuation, numbers, etc.
            .split(" ")
            .filter {
                it.length > 2 && it.lowercase() !in listOf(
                    "with", "for", "from", "gift", "daily", "matching", "best", "friend", "girlfriend", "silver", "diy", "sterling", "piece", "give"
                )
            }

        // Return only top 2 or 3 keywords to represent product
        return words.take(3).joinToString(" ")
    }

    private fun typeText(textView: TextView, fullText: String, delay: Long = 25L) {
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                if (index <= fullText.length) {
                    textView.text = fullText.substring(0, index)
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.post(runnable)
    }

//    private fun showAiDialog(productName: String, message: String) {
//        if (!isAdded) return
//
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ai_suggestion, null)
//
//        val titleText = dialogView.findViewById<TextView>(R.id.ai_dialog_title)
//        val productText = dialogView.findViewById<TextView>(R.id.ai_product_name)
//        val messageText = dialogView.findViewById<TextView>(R.id.ai_message)
//        val okBtn = dialogView.findViewById<Button>(R.id.btn_ai_ok)
//
//        val trimmedName = getShortProductName(productName)
//        productText.text = "✨ Product: $trimmedName"
//
//        messageText.text = "💬 Suggestion:\n$message"
//        typeText(messageText, "💬 Suggestion:\n$message") // typing animation
//
//        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
//            .setView(dialogView)
//
//        val customDialog = dialogBuilder.create()
//        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        okBtn.setOnClickListener {
//            customDialog.dismiss()
//        }
//
//        customDialog.show()
//    }

    private fun showAiDialog(suggestions: List<Pair<String, String>>) {
        if (!isAdded) return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_ai_suggestion, null)

        // val titleText = dialogView.findViewById<TextView>(R.id.ai_dialog_title)
        val suggestionContainer = dialogView.findViewById<LinearLayout>(R.id.ai_message)
        val okBtn = dialogView.findViewById<Button>(R.id.btn_ai_ok)
        val closeBtn = dialogView.findViewById<ImageView>(R.id.btn_close_dialog)
        // titleText.text = "🛒 Smart Cart Suggestions"

        suggestionContainer.removeAllViews() // Ensure no duplicates

        for ((productName, message) in suggestions) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_ai_suggestion, suggestionContainer, false)

            val productText = itemView.findViewById<TextView>(R.id.ai_product_name)
            val messageText = itemView.findViewById<TextView>(R.id.ai_message)

            val trimmedName = getShortProductName(productName)
            productText.text = "🛒: $trimmedName"
            messageText.text = "💬 \n$message"
            typeText(messageText, "💬 \n$message") // typing animation

            suggestionContainer.addView(itemView)
        }

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)

        val customDialog = dialogBuilder.create()
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        okBtn.setOnClickListener {
            customDialog.dismiss()
        }
        closeBtn.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }
    override fun onItemDeleteClick(product: ProductEntity) {
        cartViewModel.deleteCart(product)
        if (isAdded) {
            context?.let {
                Toast.makeText(it, "Removed From Bag", Toast.LENGTH_SHORT).show()
            }
        }
        cartViewModel.allproducts.value?.let { onCartChanged(it) }
    }
    override fun onCartChanged(cartList: List<ProductEntity>) {
        var total = 0.0
        for (item in cartList) {
            total += item.price * item.qua
        }
        totalPriceBagFrag.text = "₹${String.format("%.2f", total)}"
    }



    override fun onItemUpdateClick(product: ProductEntity) {
        cartViewModel.updateCart(product)
    }
}

