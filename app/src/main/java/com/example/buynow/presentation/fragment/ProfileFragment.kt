package com.example.buynow.presentation.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.buynow.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.http.HttpException
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


import com.example.buynow.utils.FirebaseUtils.storageReference
import com.example.buynow.data.local.room.Card.CardViewModel
import com.example.buynow.data.model.ImportContactResponse
import com.example.buynow.presentation.activity.PaymentMethodActivity
import com.example.buynow.presentation.activity.RetrofitInstance
import com.example.buynow.presentation.activity.SettingsActivity
import com.example.buynow.presentation.activity.ShipingAddressActivity
import com.google.android.gms.tasks.Continuation

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*


data class Contact(val name: String, val number: String)
data class ImportContactRequest(
    val contacts: List<Contact> // The key 'contacts' matches your backend's Pydantic model
)
class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"
    lateinit var animationView: LottieAnimationView

    lateinit var profileImage_profileFrag: CircleImageView

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    lateinit var uploadImage_profileFrag:Button
    lateinit var profileName_profileFrag:TextView
    lateinit var profileEmail_profileFrag:TextView

    private lateinit var cardViewModel: CardViewModel

    private val userCollectionRef = Firebase.firestore.collection("Users")
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    var cards: Int = 0

    lateinit var linearLayout2:LinearLayout
    lateinit var linearLayout3:LinearLayout
    lateinit var linearLayout4:LinearLayout

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage_profileFrag = view.findViewById(R.id.profileImage_profileFrag)
        val settingCd_profileFrag = view.findViewById<CardView>(R.id.settingCd_profileFrag)
        uploadImage_profileFrag = view.findViewById(R.id.uploadImage_profileFrag)
        profileName_profileFrag = view.findViewById(R.id.profileName_profileFrag)
        profileEmail_profileFrag = view.findViewById(R.id.profileEmail_profileFrag)
        animationView = view.findViewById(R.id.animationView)
        linearLayout2 = view.findViewById(R.id.linearLayout2)
        linearLayout3 = view.findViewById(R.id.linearLayout3)
        linearLayout4 = view.findViewById(R.id.linearLayout4)
        val shippingAddressCard_ProfilePage = view.findViewById<CardView>(R.id.shippingAddressCard_ProfilePage)
        val paymentMethod_ProfilePage = view.findViewById<CardView>(R.id.paymentMethod_ProfilePage)
        val cardsNumber_profileFrag:TextView = view.findViewById(R.id.cardsNumber_profileFrag)
        val importContact = view.findViewById<CardView>(R.id.import_contact)

        cardViewModel = ViewModelProviders.of(this).get(CardViewModel::class.java)

        cardViewModel.allCards.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            cards = it.size
        })

        if(cards == 0){
            cardsNumber_profileFrag.text = "You Have no Cards."
        }
        else{

        cardsNumber_profileFrag.text = "You Have "+ cards.toString() + " Cards."
        }

        shippingAddressCard_ProfilePage.setOnClickListener {
            startActivity(Intent(context, ShipingAddressActivity::class.java))
        }

        paymentMethod_ProfilePage.setOnClickListener {
            startActivity(Intent(context, PaymentMethodActivity::class.java))
        }

        hideLayout()



        uploadImage_profileFrag.visibility = View.GONE

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, launch contact picker
                Log.d(TAG, "READ_CONTACTS permission granted. Launching contact picker.")
                launchContactPicker()
            } else {
                // Permission denied, inform the user
                Log.w(TAG, "READ_CONTACTS permission denied.")
                Toast.makeText(context, "Permission denied to read contacts.", Toast.LENGTH_SHORT).show()
            }
        }
        importContact.setOnClickListener {
            Log.d(TAG, "Import Contact button clicked.")
            checkContactPermissionAndReadAllContacts()// Using Log.d for debug messages
        }

        getUserData()

        uploadImage_profileFrag.setOnClickListener {
            uploadImage()
        }

        settingCd_profileFrag.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }

        profileImage_profileFrag.setOnClickListener {

            val popupMenu: PopupMenu = PopupMenu(context,profileImage_profileFrag)

            popupMenu.menuInflater.inflate(R.menu.profile_photo_storage,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.galleryMenu ->
                        launchGallery()
                    R.id.cameraMenu ->
                        uploadImage()

                }
                true
            })
            popupMenu.show()

    }

        return view
    }
    private fun checkContactPermissionAndReadAllContacts() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), // Use requireContext() for Context
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, proceed to read all contacts
                Log.d(TAG, "READ_CONTACTS permission already granted. Reading all contacts.")
                readAllContacts()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                // Explain why the app needs this permission
                Log.i(TAG, "Showing rationale for READ_CONTACTS permission.")
                Toast.makeText(requireContext(), "This app needs contact permission to import all contacts.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            else -> {
                // Request the permission
                Log.d(TAG, "Requesting READ_CONTACTS permission.")
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
    private fun readAllContacts() {
        Toast.makeText(requireContext(),"Reading Contacts",Toast.LENGTH_SHORT).show()
        val contactsList = mutableListOf<Pair<String, String>>() // To store name and number
        var lastContactName: String? = null
        var lastContactNumber: String? = null

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val cursor = requireContext().contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameColumn = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneNumberColumn = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (it.moveToNext()) {
                val contactId = if (idColumn != -1) it.getString(idColumn) else null
                val contactName = if (nameColumn != -1) it.getString(nameColumn) else null
                val hasPhoneNumber = if (hasPhoneNumberColumn != -1) it.getInt(hasPhoneNumberColumn) else 0

                // Only proceed if contact has a phone number
                if (contactId != null && contactName != null && hasPhoneNumber > 0) {
                    var phoneNumber: String? = null
                    val phoneCursor = requireContext().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { phoneIt ->
                        val numberColumn = phoneIt.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (phoneIt.moveToFirst()) { // Get the first phone number
                            phoneNumber = if (numberColumn != -1) phoneIt.getString(numberColumn) else null
                        }
                    }
                    phoneCursor?.close()

                    if (phoneNumber != null) {
                        contactsList.add(Pair(contactName, phoneNumber?.toString()) as Pair<String, String>)
                        lastContactName = contactName
                        lastContactNumber = phoneNumber
                    }
                }
            }
        }
        cursor?.close()
        if(contactsList.size>0){
            val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token:String? = sharedPref.getString("auth_token","N/A")
            if(token == "N/A" || token == null){
                Toast.makeText(requireContext(),"Error Uploading Contacts",Toast.LENGTH_SHORT).show()
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val contactObjects = contactsList.map { pair ->
                        Contact(name = pair.first, number = pair.second)
                    }

                    val importRequest = ImportContactRequest(contacts = contactObjects)

                    val authHeader = "Bearer $token"
                    // Make the API call
                    val response: ImportContactResponse = RetrofitInstance.apiInterface.importContact(
                        authToken = authHeader,
                        request = importRequest
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Contacts imported: ${response.message}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorCode = e.code()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Import failed ($errorCode): ${errorBody ?: e.message()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    private fun hideLayout(){
        animationView.playAnimation()
        animationView.loop(true)
        linearLayout2.visibility = View.GONE
        linearLayout3.visibility = View.GONE
        linearLayout4.visibility = View.GONE
        animationView.visibility = View.VISIBLE
    }
    private fun showLayout(){
        animationView.pauseAnimation()
        animationView.visibility = View.GONE
        linearLayout2.visibility = View.VISIBLE
        linearLayout3.visibility = View.VISIBLE
        linearLayout4.visibility = View.VISIBLE
    }

    private fun getUserData() = CoroutineScope(Dispatchers.IO).launch {
        try {

            val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

            // Retrieve user data from SharedPreferences
            val userName: String? = sharedPref.getString("user_name", "N/A") // Default to "N/A" if not found
            val userEmail: String? = sharedPref.getString("user_email", "N/A") // Default to "N/A" if not found
            val userPhone: String? = sharedPref.getString("user_phone", "N/A") // Default to "N/A" if not found
            val userImage: String? = sharedPref.getString("image_url", null) // Assuming you'd store image URL if available

            Log.d(TAG, "getUserData: Retrieved Name: $userName, Email: $userEmail, Phone: $userPhone")

            withContext(Dispatchers.Main) {
                profileName_profileFrag.text = userName
                profileEmail_profileFrag.text = userEmail
                // profilePhone_profileFrag.text = userPhone // Uncomment if you have a TextView for phone

                // Load image using Glide
                if (!userImage.isNullOrEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(userImage)
                        .placeholder(R.drawable.ic_profile) // Placeholder while loading
                        .error(R.drawable.ic_profile) // Image to show if loading fails
                        .into(profileImage_profileFrag)
                } else {
                    // If no image URL, set a default placeholder
                    profileImage_profileFrag.setImageResource(R.drawable.ic_profile)
                }

                showLayout() // Call your showLayout function if it exists
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e(TAG, "Error retrieving user data from SharedPreferences", e)
                Toast.makeText(requireContext(), "Error loading profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadImage(){

        if(filePath != null){

            val ref = storageReference.child("profile_Image/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())

                    // show save...


                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{

            Toast.makeText(context, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                profileImage_profileFrag.setImageBitmap(bitmap)
                uploadImage_profileFrag.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addUploadRecordToDb(uri: String) = CoroutineScope(Dispatchers.IO).launch {

        try {

            userCollectionRef.document(firebaseAuth.uid.toString())
                .update("userImage" , uri ).await()

            withContext(Dispatchers.Main){
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }

        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context, ""+e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

}