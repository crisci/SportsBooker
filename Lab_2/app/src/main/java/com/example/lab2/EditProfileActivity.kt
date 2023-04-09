package com.example.lab_2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lab_2.entities.User
import java.io.*
import java.util.*
import kotlin.concurrent.thread

class EditProfileActivity : AppCompatActivity() {

    private lateinit var user: User

    private lateinit var profileImage : ImageView
    private lateinit var cameraImageButton: ImageButton
    private lateinit var full_name_m: EditText
    private lateinit var nickname_m: EditText
    private lateinit var description_m: EditText
    private lateinit var address_m: EditText
    private lateinit var email_m: EditText

    var image_uri: Uri? = null
    var file_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViews()

        // get the user information sended by the showProfile Activity
        // than update the content of the views
        val extras = intent.extras
        if(extras != null) {
            user = User.fromJson(extras.getString("user")!!)
        }

        updateContent()

/*        confirmButton.setOnClickListener {
            val result: Intent = Intent()
            val editedUser = User(
                full_name = full_name_m.text.toString(),
                nickname = nickname_m.text.toString(),
                address = address_m.text.toString(),
                description = description_m.text.toString(),
                email = email_m.text.toString()
                )

            result.putExtra("user", editedUser.toJson())
            setResult(Activity.RESULT_OK, result)
            finish()
        }*/
    }

    fun findViews() {
        full_name_m = findViewById(R.id.editNameSurname)
        description_m = findViewById(R.id.editDescription)
        nickname_m = findViewById(R.id.editNickname)
        address_m = findViewById(R.id.editLocation)
        email_m = findViewById(R.id.editEmail)
        profileImage = findViewById(R.id.profile_image)
        cameraImageButton = findViewById(R.id.edit_picture)

        cameraImageButton.setOnClickListener { popupMenuSetup() }
    }

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
        //TODO: do the same for the other fields to be modified
        //in alternative you can create a fake user that every time you save this user as the ShowProfileActivity
        //when confirm is pressed than the user is commited and sended back
        //you can use editedUser
        full_name_m.setText(savedInstanceState.getString("full_name"))
        nickname_m.setText(savedInstanceState.getString("nickname"))
        description_m.setText(savedInstanceState.getString("description"))
        address_m.setText(savedInstanceState.getString("address"))
        email_m.setText(savedInstanceState.getString("email"))

        file_name = savedInstanceState.getString("image")
        val inputStream = applicationContext.openFileInput(file_name)
        val rotated = BitmapFactory.decodeStream(inputStream)
        //val rotated = rotateBitmap(inputImageBitmap!!)
        profileImage.setImageBitmap(rotated)

/*
        user = User(
            full_name_m.text.toString(),
            nickname_m.text.toString(),
            address_m.text.toString(),
            description_m.text.toString())
*/

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO: do the same for the other fields
        outState.putString("full_name", full_name_m.text.toString())
        outState.putString("nickname", nickname_m.text.toString())
        outState.putString("description", description_m.text.toString())
        outState.putString("address", address_m.text.toString())
        outState.putString("email", email_m.text.toString())
        outState.putString("image", file_name)
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
                if (full_name_m.text.toString() == "" ||
                    nickname_m.text.toString() == "" ||
                    description_m.text.toString() == "" ||
                    address_m.text.toString() == "" ||
                    email_m.text.toString() == "") {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    saveData()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

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
            val inputStream = applicationContext.openFileInput(file_name)
            val rotated = BitmapFactory.decodeStream(inputStream)
            //val rotated = rotateBitmap(inputImageBitmap!!)
            rotated?.let {
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
                    val fileName = "image.jpg"
                    // Create an outputStream to write the image data to a file in internal storage.
                    // The first parameter is the name of the file, and the second parameter is the
                    // file mode which determines the access level of the file. We use MODE_PRIVATE
                    // to make the file accessible only to our app.
                    // This operation is time-consuming, so we define a thread.
                    thread {
                        val outputStream = applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE)
                        // We compress the bitmap data to PNG format and write it to the outputStream
                        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    }

                    file_name = fileName
                    println(file_name)
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
                    val fileName = "image.jpg"
                    // Create an outputStream to write the image data to a file in internal storage.
                    // The first parameter is the name of the file, and the second parameter is the
                    // file mode which determines the access level of the file. We use MODE_PRIVATE
                    // to make the file accessible only to our app.
                    // This operation is time-consuming, so we define a thread.
                    thread {
                        val outputStream =
                            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE)
                        // We compress the bitmap data to PNG format and write it to the outputStream
                        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    }
                    file_name = fileName
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

    //TODO Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
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
        val sharedPreference =  getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        val result: Intent = Intent()
        editor.putString("full_name", full_name_m.text.toString())
        editor.putString("nickname", nickname_m.text.toString())
        editor.putString("description", description_m.text.toString())
        editor.putString("address", address_m.text.toString())
        editor.putString("email", email_m.text.toString())
        editor.putString("image", file_name)
        editor.apply()

        val editedUser = User(
            full_name = full_name_m.text.toString(),
            nickname = nickname_m.text.toString(),
            address = address_m.text.toString(),
            description = description_m.text.toString(),
            email = email_m.text.toString(),
            image = file_name
        )
        result.putExtra("user", editedUser.toJson())
        setResult(Activity.RESULT_OK, result)

        finish()
    }
}