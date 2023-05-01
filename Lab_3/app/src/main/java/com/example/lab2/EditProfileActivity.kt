package com.example.lab2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cunoraz.tagview.*
import com.example.lab2.entities.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.thread

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private lateinit var user: User
    private var cameraHasFinished: Boolean = true


    private val listAllInterests: List<Sport> = Sport.values().toList()

    private lateinit var profileImage : ImageView
    private lateinit var cameraImageButton: ImageButton
    private lateinit var full_name_m: EditText
    private lateinit var nickname_m: EditText
    private lateinit var description_m: EditText
    private lateinit var address_m: EditText
    private lateinit var email_m: EditText
    private lateinit var birthday_m: EditText
    private lateinit var tagGroup: TagView
    private lateinit var confirmButton: Button

    var image_uri: Uri? = null
    var file_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViews()


        supportActionBar?.title = "Edit Profile"
        supportActionBar?.elevation = 0f

        // Get the user information sended by the showProfile Activity,
        // than update the content of the views
        val extras = intent.extras
        if(extras != null) {
            user = User.fromJson(extras.getString("user")!!)
        }

        initDatePicker()
        updateContent()

        var valid = true

        //TODO Validation
        confirmButton.setOnClickListener {
            if (full_name_m.text.toString().trim() == "" ||
                nickname_m.text.toString().trim() == "" ||
                description_m.text.toString().trim() == "" ||
                address_m.text.toString().trim() == "" ||
                email_m.text.toString().trim() == "" ||
                birthday_m.text.toString().trim() == "") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                valid = false
            }
            else if(!isValidEmail(email_m.text.trim())) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                valid = false
            }
            else if(nickname_m.text.contains(" ")) {
                Toast.makeText(this, "Please enter a valid nickname", Toast.LENGTH_SHORT).show()
                valid = false
            }
            else if(description_m.text.length > 150) {
                Toast.makeText(this, "The description must have at most 150 characters", Toast.LENGTH_SHORT).show()
                valid = false
            }

            if (valid) saveData()
        }

        setupTags()
        tagGroup.setOnTagClickListener { tag, position ->
            var uppercaseTagName = tag.text.uppercase(Locale.getDefault()) // i.e. from "Soccer" to "SOCCER", which is the constant in the enum
            if (user.interests.any { it.name == uppercaseTagName }) {
                user.interests = user.interests.filterNot{it.name == uppercaseTagName}.toMutableList()
                user.statistics.remove(Sport.valueOf(uppercaseTagName))
                setupTags()
            }
            else {
                if(user.interests.size < 3) {
                    user.interests.add(Sport.valueOf(uppercaseTagName))
                    user.statistics.put(Sport.valueOf(uppercaseTagName), Statistic(
                        sport = Sport.valueOf(uppercaseTagName),
                        gamesPlayed = 0,
                        gamesWon = 0,
                        gamesLost = 0,
                        gamesDrawn = 0
                    ))
                    setupTags()
                }
                else {
                    Toast.makeText(this, "You can add at most 3 interests", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    //TODO This function fills the TagView with all the interests:
    // - The interests of the user are green
    // - The other interests are white
    // This method must be called everytime we make a change in our interests to update the colors.
    private fun setupTags() {
        // We want to make a union between the interests of the user and all the other interests
        tagGroup.addTags(listAllInterests.union(user.interests).map {currentInterest ->
            /*
            * The enum constants are in uppercase, like "SOCCER". We want a tag like "Soccer".
            * The following code is equivalent to inputString.toLowerCase().capitalize(),
            * however these functions are deprecated. The IDE suggested the code below instead.
            * */
            var tag = Tag(currentInterest.name.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
            tag.tagTextSize = 18F

            // If the list of the user's interests contains the current interest, it should be green
            if (user.interests.contains(currentInterest)) {
                tag.layoutColor = ContextCompat.getColor(this, R.color.bright_green)
                tag.tagTextColor = Color.WHITE
            }
            else {
                // otherwise it is white
                tag.layoutColor = Color.WHITE
                tag.tagTextColor = Color.BLACK
                tag.layoutBorderColor = Color.BLACK
                tag.layoutBorderSize = 1F
            }
            tag
        })
    }

    //TODO A function that calls all the findViewById() methods for each UI element
    fun findViews() {
        full_name_m = findViewById(R.id.editNameSurname)
        description_m = findViewById(R.id.editDescription)
        nickname_m = findViewById(R.id.editNickname)
        address_m = findViewById(R.id.editLocation)
        email_m = findViewById(R.id.editEmail)
        birthday_m = findViewById(R.id.editBod)
        profileImage = findViewById(R.id.profile_image)
        cameraImageButton = findViewById(R.id.edit_picture)
        confirmButton = findViewById(R.id.confirm_button)
        tagGroup = findViewById(R.id.tag_group)

        cameraImageButton.setOnClickListener { popupMenuSetup() }
    }

    //TODO This is the popup menu that is displayed as we click on the ImageButton overlapped to the profile image.
    // It contains two options: "Camera" or "Library".
    // - If we press on the first option, the app checks and asks the permission to open the camera
    // and then the user is ready to take a photo.
    // - If we press on the second option, the app opens the photo library and the user can pick one photo.
    private fun popupMenuSetup() {
        val popupMenu = PopupMenu(this,cameraImageButton)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.take_photo -> {
                    checkPermissionsAndOpenCamera()
                    true
                }
                R.id.choose_photo_from_gallery -> {
                    openPhotoLibrary()
                    true
                }
                else -> true
            }
        }
        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            popupMenu.show()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        full_name_m.setText(savedInstanceState.getString("full_name"))
        nickname_m.setText(savedInstanceState.getString("nickname"))
        description_m.setText(savedInstanceState.getString("description"))
        address_m.setText(savedInstanceState.getString("address"))
        email_m.setText(savedInstanceState.getString("email"))
        birthday_m.setText(savedInstanceState.getString("birthday"))

        // Open and load the photo
        file_name = savedInstanceState.getString("image")
        val inputStream = applicationContext.openFileInput(file_name)
        val rotated = BitmapFactory.decodeStream(inputStream)
        profileImage.setImageBitmap(rotated)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("full_name", full_name_m.text.toString())
        outState.putString("nickname", nickname_m.text.toString())
        outState.putString("description", description_m.text.toString())
        outState.putString("address", address_m.text.toString())
        outState.putString("email", email_m.text.toString())
        outState.putString("image", file_name)
        outState.putString("birthday", birthday_m.text.toString())

        /*
        * We are using the Gson library to serialize a user's interests object into a JSON string,
        * and then storing it in a Bundle as a string with the key "interests".
        * */

        val gsonInterests = Gson()
        val jsonInterests = gsonInterests.toJson(user.interests)
        outState.putString("interests", jsonInterests)

        val gsonStatistics = Gson()
        val jsonStatistics = gsonStatistics.toJson(user.statistics)
        outState.putString("statistics", jsonStatistics)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.backmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.backmenu -> {
                finish()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    //TODO This function fills the fields
    private fun updateContent() {
        full_name_m.setText(user.full_name)
        nickname_m.setText(user.nickname)
        description_m.setText(user.description)
        address_m.setText(user.address)
        email_m.setText(user.email)

        if (user.image == null) {
            profileImage.setBackgroundResource(R.drawable.profile_picture)
        }
        else {
            file_name = user.image
            // Open and load the photo
            val inputStream = applicationContext.openFileInput(file_name)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.let {
                // Set the new image to the ImageView
                profileImage.setImageBitmap(it)
            }
        }

    }

    //TODO capture the image using camera and display it
    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
            if (it.resultCode === RESULT_OK) {
                val inputImage = uriToBitmap(image_uri!!)
                val rotated = rotateBitmap(inputImage!!)
                rotated?.let {
                    // Set the new image to the ImageView
                    profileImage.setImageBitmap(it)
                    file_name = "image.jpg"
                    // Create an outputStream to write the image data to a file in internal storage.
                    // The first parameter is the name of the file, and the second parameter is the
                    // file mode which determines the access level of the file. We use MODE_PRIVATE
                    // to make the file accessible only to our app.
                    // This operation is time-consuming, so we define a thread.
                    thread {
                        cameraHasFinished = false
                        val outputStream = applicationContext.openFileOutput(file_name, Context.MODE_PRIVATE)
                        // We compress the bitmap data to PNG format and write it to the outputStream
                        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        cameraHasFinished = true
                    }
                }
            }
        }
    )

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
            if (it.getResultCode() === RESULT_OK) {
                image_uri = it.data?.data
                val inputImage = uriToBitmap(image_uri!!)
                val rotated = rotateBitmap(inputImage!!)
                rotated?.let {
                    // Set the new image to the ImageView
                    profileImage.setImageBitmap(it)
                    file_name = "image.jpg"
                    // Create an outputStream to write the image data to a file in internal storage.
                    // The first parameter is the name of the file, and the second parameter is the
                    // file mode which determines the access level of the file. We use MODE_PRIVATE
                    // to make the file accessible only to our app.
                    // This operation is time-consuming, so we define a thread.
                    thread {
                        cameraHasFinished = false
                        val outputStream =
                            applicationContext.openFileOutput(file_name, Context.MODE_PRIVATE)
                        // We compress the bitmap data to PNG format and write it to the outputStream
                        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        cameraHasFinished = true
                    }
                }
            }
        }
    )

    //TODO takes URI of the image and returns bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    //TODO Most phone cameras are landscape, meaning if you take the photo in portrait,
    // the resulting photos will be rotated 90 degrees. So we need a function to get the orientation
    // of the photo and to rotate it accordingly.
    @SuppressLint("Range")
    fun rotateBitmap(input: Bitmap): Bitmap? {
        val orientationColumn =
            arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur: Cursor? = contentResolver.query(image_uri!!, orientationColumn, null, null, null)
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    }
    private fun checkPermissionsAndOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf<String>(
                    android.Manifest.permission.CAMERA
                )
                requestPermissions(permission, 112)
            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 112) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    private fun openPhotoLibrary() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(galleryIntent)
    }

    private fun saveData() {

        /// TODO if camera thread is still running, it should appear a loading icon

        while (!cameraHasFinished) {
        }
        confirmButton.text = "Confirm"
        val sharedPreference =  getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        val result: Intent = Intent()
        editor.putString("full_name", full_name_m.text.toString().trim())
        editor.putString("nickname", nickname_m.text.toString().trim())
        editor.putString("description", description_m.text.toString().trim())
        editor.putString("address", address_m.text.toString().trim())
        editor.putString("email", email_m.text.toString().trim())
        editor.putString("image", file_name)
        editor.putString("birthday", birthday_m.text.toString().trim())

        val gsonInterests = Gson()
        val jsonInterests = gsonInterests.toJson(user.interests)
        editor.putString("interests", jsonInterests)

        val gsonStatistics = Gson()
        val jsonStatistics = gsonStatistics.toJson(user.statistics)
        editor.putString("statistics", jsonStatistics)

        editor.apply()

        val editedUser = User(
            full_name = full_name_m.text.toString().trim(),
            nickname = nickname_m.text.toString().trim(),
            address = address_m.text.toString().trim(),
            description = description_m.text.toString().trim(),
            email = email_m.text.toString().trim(),
            image = file_name,
            birthday = user.birthday,
            interests = user.interests,
            statistics = user.statistics
        )
        result.putExtra("user", editedUser.toJson())
        setResult(Activity.RESULT_OK, result)

        finish()
    }

    //TODO This function initializes the DatePickerDialog
    private fun initDatePicker() {
        birthday_m.setText("${user.birthday.dayOfMonth}/${user.birthday.monthValue}/${user.birthday.year}")
        birthday_m.setOnClickListener {
            val c = user.birthday
            val uYear = c.year
            val uMonth =  c.monthValue
            val uDay =  c.dayOfMonth

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                    birthday_m.setText(dat)
                    user.birthday = LocalDate.of(year, monthOfYear+1, dayOfMonth)
                },
                uYear,
                uMonth,
                uDay
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog.show()
        }
    }
}