package com.example.buynow.presentation.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buynow.presentation.adapter.ProductAdapter
import com.example.buynow.R
import com.example.buynow.data.model.Product
import com.example.buynow.utils.DefaultCard.GetDefCard
import com.example.buynow.utils.Extensions.cardXXGen
import com.example.buynow.utils.Extensions.toast
import com.example.buynow.data.local.room.CartViewModel
import com.example.buynow.data.local.room.ProductEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import android.app.AlertDialog
import com.example.buynow.data.model.User
import com.example.buynow.data.model.PurchaseDisplay
import android.graphics.Color
import android.view.Gravity
// For animation interpolator
import android.view.animation.AccelerateDecelerateInterpolator

// For bold text (Typeface)
import android.graphics.Typeface
import android.util.Log

// For image transition using Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.buynow.data.model.ProductById
import com.example.buynow.presentation.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class ProductDetailsActivity : AppCompatActivity() {

    var productIndex: Int = 0
    lateinit var ProductFrom: String
    private lateinit var cartViewModel: CartViewModel
    lateinit var productImage_ProductDetailsPage: ImageView
    lateinit var backIv_ProfileFrag: ImageView
    lateinit var productName_ProductDetailsPage: TextView
    lateinit var productPrice_ProductDetailsPage: TextView
    lateinit var productBrand_ProductDetailsPage: TextView
    lateinit var productDes_ProductDetailsPage: TextView
    lateinit var RatingProductDetails: TextView
    lateinit var productRating_singleProduct: RatingBar

    lateinit var loadingDialog: LoadingDialog

    lateinit var RecomRecView_ProductDetailsPage: RecyclerView
    lateinit var newProductAdapter: ProductAdapter
    lateinit var newProduct: ArrayList<Product>

    lateinit var pName: String
    var qua: Int = 1
    var pPrice: Int = 0
    var pPid by Delegates.notNull<Int>()
    lateinit var pImage: String

    lateinit var cardNumber: String
    var productId :Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        productId = intent.getIntExtra("ProductIndex", -1)
        ProductFrom = intent.getStringExtra("ProductFrom").toString()
        Log.d("ProductId",productIndex.toString())
        productImage_ProductDetailsPage = findViewById(R.id.productImage_ProductDetailsPage)
        productName_ProductDetailsPage = findViewById(R.id.productName_ProductDetailsPage)
        productPrice_ProductDetailsPage = findViewById(R.id.productPrice_ProductDetailsPage)
        productBrand_ProductDetailsPage = findViewById(R.id.productBrand_ProductDetailsPage)
        productDes_ProductDetailsPage = findViewById(R.id.productDes_ProductDetailsPage)
        productRating_singleProduct = findViewById(R.id.productRating_singleProduct)
        RatingProductDetails = findViewById(R.id.RatingProductDetails)
        RecomRecView_ProductDetailsPage = findViewById(R.id.RecomRecView_ProductDetailsPage)
        backIv_ProfileFrag = findViewById(R.id.backIv_ProfileFrag)
        val addToCart_ProductDetailsPage: Button = findViewById(R.id.addToCart_ProductDetailsPage)
        val shippingAddress_productDetailsPage:LinearLayout = findViewById(R.id.shippingAddress_productDetailsPage)
        val cardNumberProduct_Details:TextView = findViewById(R.id.cardNumberProduct_Details)

        cardNumber = GetDefCard()

        if(cardNumber == "" || cardNumber == null){
            cardNumberProduct_Details.text = "You Have No Cards"
        }
        else{
            cardNumberProduct_Details.text = cardXXGen(cardNumber)
        }


        shippingAddress_productDetailsPage.setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }


        newProduct = arrayListOf()
        setProductData()
        setRecData()

        RecomRecView_ProductDetailsPage.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        RecomRecView_ProductDetailsPage.setHasFixedSize(true)
        newProductAdapter = ProductAdapter(newProduct, this)
        RecomRecView_ProductDetailsPage.adapter = newProductAdapter

        backIv_ProfileFrag.setOnClickListener {
            onBackPressed()
        }
        loadRecentlyBoughtContacts()

        addToCart_ProductDetailsPage.setOnClickListener {

            val bottomSheetDialod = BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme
            )

            val bottomSheetView = LayoutInflater.from(applicationContext).inflate(
                R.layout.fragment_add_to_bag,
                findViewById<ConstraintLayout>(R.id.bottomSheet)
            )

            bottomSheetView.findViewById<View>(R.id.addToCart_BottomSheet).setOnClickListener {

                pPrice *= bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom).text.toString()
                    .toInt()
                addProductToBag()
                bottomSheetDialod.dismiss()
            }

            bottomSheetView.findViewById<LinearLayout>(R.id.minusLayout).setOnClickListener {
                if(bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom).text.toString()
                        .toInt() > 1){
                    qua--
                    bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom).setText(qua.toString())
                }
            }

            bottomSheetView.findViewById<LinearLayout>(R.id.plusLayout).setOnClickListener {
                if(bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom).text.toString()
                        .toInt() < 10){
                    qua++
                    bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom).setText(qua.toString())
                }
            }

            bottomSheetDialod.setContentView(bottomSheetView)
            bottomSheetDialod.show()

        }

    }

    private fun loadRecentlyBoughtContacts() {
        val recentBuyers = listOf(
            PurchaseDisplay(User("Priya Sharma", "https://randomuser.me/api/portraits/women/1.jpg"), "Red Dress", "2 hours ago"),
            PurchaseDisplay(User("Amit Singh", "https://randomuser.me/api/portraits/men/2.jpg"), "Leather Wallet", "5 hours ago"),
            PurchaseDisplay(User("Nikita Rao", "https://randomuser.me/api/portraits/women/3.jpg"), "Sunglasses", "1 day ago"),
            PurchaseDisplay(User("Raj Mehta", "https://randomuser.me/api/portraits/men/4.jpg"), "Sneakers", "2 days ago"),
            PurchaseDisplay(User("Ishita Verma", "https://randomuser.me/api/portraits/women/5.jpg"), "Wrist Watch", "3 days ago")
        )

        val container = findViewById<LinearLayout>(R.id.recentBuyersContainer)
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)
        val rowLayout = inflater.inflate(R.layout.item_recent_buyers_row, container, false) as LinearLayout

        val avatarSize = 110
        val overlapMargin = -45

        recentBuyers.forEach { purchase ->
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(avatarSize, avatarSize)
            params.setMargins(0, 0, overlapMargin, 0)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(4, 4, 4, 4)
            imageView.setBackgroundResource(R.drawable.avatar_border)
            imageView.elevation = 6f

            Glide.with(this)
                .load(purchase.user.userImage)
                .placeholder(R.drawable.placeholder_avatar)
                .transition(DrawableTransitionOptions.withCrossFade())
                .circleCrop()
                .into(imageView)

            rowLayout.addView(imageView)
        }

        // ➕ Add final plus sign overlapping last avatar
        val plusView = TextView(this)
        val plusParams = LinearLayout.LayoutParams(avatarSize, avatarSize)
        plusParams.setMargins(0, 0, 0, 0)
        plusView.layoutParams = plusParams
        plusView.text = "+"
        plusView.textSize = 22f
        plusView.setTextColor(Color.WHITE)
        plusView.setBackgroundColor(Color.TRANSPARENT)
        plusView.gravity = Gravity.CENTER
        plusView.elevation = 6f
        plusView.setTypeface(null, Typeface.BOLD)

        rowLayout.addView(plusView)
        rowLayout.alpha = 0f
        rowLayout.translationY = 50f

        rowLayout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        container.addView(rowLayout)

        // 🟦 Click handler to show dialog
        val section = findViewById<View>(R.id.recentlyBoughtSection)
        val showDialog = { showBuyerDialog(recentBuyers) }
        section.setOnClickListener { showDialog() }
        container.setOnClickListener { showDialog() }
    }




    private fun showBuyerDialog(recentBuyers: List<PurchaseDisplay>) {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_recent_buyers, null)
        val closeIcon = view.findViewById<ImageView>(R.id.closeIcon)
        val container = view.findViewById<LinearLayout>(R.id.buyerDialogList)

        recentBuyers.forEach { buyer ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 20)
                layoutParams = params
                setBackgroundResource(R.drawable.buyer_row_bg)
            }

            val text = TextView(this).apply {
                text = "👤 ${buyer.user.userName}   •   🕒 ${buyer.timeAgo}"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.white))
                typeface = resources.getFont(R.font.metropolis_regular)
            }

            row.addView(text)
            container.addView(row)
        }

        closeIcon.setOnClickListener { dialog.dismiss() }

        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }







    private fun addProductToBag() {

        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)

        cartViewModel.insert(ProductEntity(pName, qua, pPrice, pPid, pImage))
        toast("Add to Bag Successfully")
    }

    fun getJsonData(context: Context, fileName: String): String? {


        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }

        return jsonString
    }

    private fun setProductData() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPref.getString("auth_token", null)
        Log.d("TokenAuth",authToken.toString())
        var pr : Product? = null
        if(authToken != null){
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    Log.d("TRYBLOCK",productId.toString())
                    val response :ProductById = RetrofitInstance.apiInterface.getProductById(
                        productId = productId,authToken="Bearer $authToken"
                    )
                    withContext(Dispatchers.Main) {
                        val pr = response.product
                        Glide.with(applicationContext)
                            .load(pr.productImage)
                            .into(productImage_ProductDetailsPage)

                        productName_ProductDetailsPage.text = pr.productName
                        productPrice_ProductDetailsPage.text = "$${pr.productPrice}"
                        productBrand_ProductDetailsPage.text = pr.productBrand
                        productDes_ProductDetailsPage.text = pr.productDes
                        productRating_singleProduct.rating = pr.productRating.toFloat()
                        RatingProductDetails.text = "${pr.productRating} Rating on this Product."

                        pName = pr.productName
                        pPrice = pr.productPrice.toInt()
                        pPid = pr.productId
                        pImage = pr.productImage
                    }
                    Log.d("ProductDataId",pr.toString())
                } catch (e:Exception){
                    withContext(Dispatchers.Main) {
//                        loadingDialog.dismissDialog() // Dismiss dialog on error
                        Toast.makeText(
                            applicationContext, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace() // Log the error for debugging
                    }
                }
            }
        }

//        var fileJson: String = ""
//
//        if (ProductFrom.equals("Cover")) {
//            fileJson = "CoverProducts.json"
//        }
//        if (ProductFrom.equals("New")) {
//            fileJson = "NewProducts.json"
//        }
//
//
//        val jsonFileString = this.let {
//
//            getJsonData(it, fileJson)
//        }
//
//        val gson = Gson()
//
//
//        val listCoverType = object : TypeToken<List<Product>>() {}.type
//
//        var coverD: List<Product> = gson.fromJson(jsonFileString, listCoverType)
//
//        Glide.with(applicationContext)
//            .load(coverD[productIndex].productImage)
//            .into(productImage_ProductDetailsPage)
//
//            productName_ProductDetailsPage.text = pr?.productName ?: coverD[productIndex].productName
//            productPrice_ProductDetailsPage.text = "$" + (pr?.productPrice ?: 0)
//            productBrand_ProductDetailsPage.text = pr?.productBrand ?: coverD[productIndex].productBrand
//            productDes_ProductDetailsPage.text = pr?.productDes ?: coverD[productIndex].productDes
//            productRating_singleProduct.rating = coverD[productIndex].productRating
//            RatingProductDetails.text = coverD[productIndex].productRating.toString() + " Rating on this Product."
//
//            pName = coverD[productIndex].productName
//            pPrice = coverD[productIndex].productPrice.toInt()
//            pPid = coverD[productIndex].productId
//            pImage = pr?.productImage?: coverD[productIndex].productImage

    }

    private fun setRecData() {

        var fileJson: String = ""

        if (ProductFrom.equals("Cover")) {
            fileJson = "NewProducts.json"
        }
        if (ProductFrom.equals("New")) {
            fileJson = "CoverProducts.json"
        }
    }
}

