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

class EditProfileActivity : AppCompatActivity() {

    private lateinit var user: User

    private lateinit var profileImage : ImageView
    private lateinit var cameraImageButton: ImageButton
    private lateinit var full_name_m: EditText
    private lateinit var nickname_m: EditText
    private lateinit var description_m: EditText
    private lateinit var address_m: EditText
    private lateinit var email_m: EditText
    private lateinit var confirmButton: Button

    var image_uri: Uri? = null

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

        confirmButton.setOnClickListener {
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
        }
    }

    fun findViews() {
        full_name_m = findViewById(R.id.editNameSurname)
        description_m = findViewById(R.id.editDescription)
        nickname_m = findViewById(R.id.editNickname)
        address_m = findViewById(R.id.editLocation)
        email_m = findViewById(R.id.editEmail)
        profileImage = findViewById(R.id.profile_image)
        cameraImageButton = findViewById(R.id.edit_picture)
        confirmButton = findViewById(R.id.confirm_button)

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
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

        user = User(
            full_name_m.text.toString(),
            nickname_m.text.toString(),
            address_m.text.toString(),
            description_m.text.toString())

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO: do the same for the other fields
        outState.putString("full_name", full_name_m.text.toString())
        outState.putString("nickname", nickname_m.text.toString())
        outState.putString("description", description_m.text.toString())
        outState.putString("address", address_m.text.toString())
        outState.putString("email", email_m.text.toString())
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

    private fun updateContent() {
        full_name_m.setText(user.full_name)
        nickname_m.setText(user.nickname)
        description_m.setText(user.description)
        address_m.setText(user.address)
        email_m.setText(user.email)
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
                }
            }
        }
    )

    //TODO Registers a photo picker activity launcher in single-select mode.
    private var pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            image_uri = uri
            profileImage.setImageURI(image_uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

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
                PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf<String>(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
}