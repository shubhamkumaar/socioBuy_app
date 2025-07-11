package com.example.buynow.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.buynow.R
import com.example.buynow.data.model.Product
import com.example.buynow.presentation.activity.VisualSearchActivity
import com.example.buynow.presentation.adapter.CoverProductAdapter
import com.example.buynow.presentation.adapter.ProductAdapter
import com.example.buynow.presentation.viewmodel.ProductViewModel

class HomeFragment : Fragment() {

    private lateinit var coverRecView: RecyclerView
    private lateinit var animationView: LottieAnimationView
    private lateinit var productViewModel: ProductViewModel
    private var TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Views
        coverRecView = view.findViewById(R.id.coverRecView)
        animationView = view.findViewById(R.id.animationView)

        val visualSearchBtn: ImageView = view.findViewById(R.id.visualSearchBtn_homePage)
        visualSearchBtn.setOnClickListener {
            startActivity(Intent(context, VisualSearchActivity::class.java))
        }

        hideLayout()

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        productViewModel.loadProducts()

        productViewModel.productResult.observe(viewLifecycleOwner) { result ->
            val (categories, coverProducts) = result

            setupCoverProducts(coverProducts)

            // Only take first 5 categories (sorted alphabetically or however you like)
            val limitedCategories = categories.entries.take(5)

            // Map to your fixed section IDs
            val sectionIds = listOf(
                R.id.category_underwear,
                R.id.category_tools,
                R.id.category_office,
                R.id.category_men,
                R.id.category_jewelry
            )

            // Display only up to 5 category sections
            for ((index, entry) in limitedCategories.withIndex()) {
                val (categoryName, productList) = entry
                setCategorySection(view, sectionIds[index], categoryName, productList)
            }

            // Hide unused section layouts if API returns < 5 categories
            for (i in limitedCategories.size until sectionIds.size) {
                view.findViewById<View>(sectionIds[i])?.visibility = View.GONE
            }

            showLayout()
        }


        return view
    }

    private fun setupCoverProducts(coverProducts: List<Product>) {
        val adapter = CoverProductAdapter(requireContext(), ArrayList(coverProducts))
        coverRecView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        coverRecView.adapter = adapter
    }

    private fun setCategorySection(root: View, sectionId: Int, title: String, products: List<Product>?) {
        val sectionView = root.findViewById<View>(sectionId)
        if (sectionView == null) {
            Log.e(TAG, "Section view null for ID=$sectionId ($title)")
            return
        }

        val titleView = sectionView.findViewById<TextView>(R.id.categoryTitle)
        val recyclerView = sectionView.findViewById<RecyclerView>(R.id.categoryRecycler)
        if (titleView == null || recyclerView == null) {
            Log.e(TAG, "Missing title or recycler view in section '$title'")
            return
        }

        val productCount = products?.size ?: 0
        Log.d(TAG, "$title → Products count: $productCount")

        titleView.text = title
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        try {
            recyclerView.adapter = ProductAdapter(products ?: emptyList(), requireContext(),"Category")
        } catch (e: Exception) {
            Log.e(TAG, "Adapter exception in '$title':", e)
        }
    }




    private fun hideLayout() {
        animationView.playAnimation()
        animationView.loop(true)
        animationView.visibility = View.VISIBLE
        coverRecView.visibility = View.GONE
    }

    private fun showLayout() {
        animationView.pauseAnimation()
        animationView.visibility = View.GONE
        coverRecView.visibility = View.VISIBLE
    }
}
