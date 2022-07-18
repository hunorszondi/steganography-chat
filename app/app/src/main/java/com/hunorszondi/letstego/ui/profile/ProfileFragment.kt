package com.hunorszondi.letstego.ui.profile

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.ui.auth.AuthActivity
import com.hunorszondi.letstego.ui.auth.RegisterFragment
import com.hunorszondi.letstego.utils.DialogFactory
import com.hunorszondi.letstego.utils.ImageRotationUtil
import com.hunorszondi.letstego.utils.ResourceUtil
import com.hunorszondi.letstego.viewModels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI for displaying and editing user profile information
 */
class ProfileFragment : BaseFragment() {
    private val REQUEST_TAKE_PHOTO: Int = 100
    private val REQUEST_SELECT_IMAGE_FROM_GALLERY: Int = 101
    private val REQUEST_PERMISSION: Int = 200

    private var currentPhotoPath: String? = null
    private var originalPicture: Bitmap? = null
    private var functionToCallAfterPermission: (()->Unit)? = null

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        initView()
        initData()
    }

    /**
     * Loading data into UI elements
     */
    private fun initData() {
        val user = Session.instance.currentUser!!

        val userText = "Hello ${user.userName}!"
        usernameTextView.text = userText

        passwordInput.setText(user.password)
        displayNameInput.setText(user.displayName)
        emailInput.setText(user.email)

        Glide
            .with(requireContext())
            .load(user.photo?:R.drawable.ic_profile_placeholder)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(profilePicture)
    }

    /**
     * Initializing View, defining listeners
     */
    private fun initView() {
        navigationHeader.setTitleVisibility(View.VISIBLE)
            .setTitle(ResourceUtil.instance.getString(R.string.profile))
            .setButtonVisibility(View.VISIBLE)
            .setButtonIcon(R.drawable.ic_arrow_back)
            .setButtonClickListener(View.OnClickListener { requireActivity().onBackPressed() })

        changeProfilePictureButton.setOnClickListener { openImageSelector() }

        updateButton.setOnClickListener {
            showLoading()
            viewModel.updateProfile(passwordInput.text.toString(),
                displayNameInput.text.toString(),
                emailInput.text.toString(), currentPhotoPath) {_, message ->
                requireActivity().runOnUiThread {
                    cancelLoading()
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        signOutButton.setOnClickListener {
            logout()
        }

        deleteProfileButton.setOnClickListener {
            DialogFactory.makeMessage(requireContext(),
                ResourceUtil.instance.getString(R.string.delete_profile),
                ResourceUtil.instance.getString(R.string.are_you_sure_delete_profile),
                ResourceUtil.instance.getString(R.string.yes),
                ResourceUtil.instance.getString(R.string.no),
                DialogInterface.OnClickListener {_, _ ->
                    viewModel.deleteProfile { status, message ->
                        if(status) {
                            logout()
                        } else {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                DialogInterface.OnClickListener {_, _ -> }).show()
        }
    }

    private fun logout() {
        Session.instance.logout()
        startActivity(Intent(requireContext(), AuthActivity::class.java))
        requireActivity().finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                /**
                 * After an image has been loaded from gallery, here will be handled the new data
                 */
                REQUEST_SELECT_IMAGE_FROM_GALLERY -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val imageUri = data!!.data
                        originalPicture = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                        originalPicture = ImageRotationUtil()
                            .rotateImageIfRequired(requireContext(), originalPicture!!, imageUri!!, true)
                        createImageFileFromGallery(originalPicture!!)
                        profilePicture.setImageBitmap(originalPicture)
                    } else {
                        Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
                    }
                }
                /**
                 * After an image has been captured with camera, here will be handled the new data
                 */
                REQUEST_TAKE_PHOTO -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val file = File(currentPhotoPath)
                        originalPicture = MediaStore.Images.Media
                            .getBitmap(requireContext().contentResolver, Uri.fromFile(file))
                        originalPicture = ImageRotationUtil()
                            .rotateImageIfRequired(requireContext(),
                                originalPicture!!,
                                Uri.parse(currentPhotoPath),
                                false
                            )
                        profilePicture.setImageBitmap(originalPicture)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.d("ProfileFragment", ex.toString())
            Toast.makeText(context, "Image can not be loaded", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val directory = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.toURI())
        FileUtils.deleteDirectory(directory)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
                } else {
                    functionToCallAfterPermission?.invoke()
                }
            }
        }
    }

    /**
     * Opens the image source selector dialog
     */
    private fun openImageSelector() {
        val alertDialog = DialogFactory.makeMessage(requireContext(),
            ResourceUtil.instance.getString(R.string.select_image),
            ResourceUtil.instance.getString(R.string.choose_import_method),
            ResourceUtil.instance.getString(R.string.gallery),
            ResourceUtil.instance.getString(R.string.camera),
            DialogInterface.OnClickListener { _, _ -> selectImageFromGalleryWithCheck()  },
            DialogInterface.OnClickListener { _, _ -> takePhotoWithCheck()  })
        alertDialog.show()
    }

    /**
     * Opens the gallery to select an image with permission check
     */
    private fun selectImageFromGalleryWithCheck() {
        if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            selectImageFromGallery()
        } else {
            makePermissionRequest(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), ::selectImageFromGallery)
        }
    }

    /**
     * Opens the gallery to select an image
     */
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_SELECT_IMAGE_FROM_GALLERY)
        }
    }

    /**
     * Opens the camera module to capture an image with permission check
     */
    private fun takePhotoWithCheck() {
        if(checkPermission(Manifest.permission.CAMERA)) {
            takePhoto()
        } else {
            makePermissionRequest(arrayOf(Manifest.permission.CAMERA), ::takePhoto)
        }
    }

    /**
     * Opens the camera module to capture an image
     */
    private fun takePhoto() {
        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File = createImageFile("temp_profile_picture")
            val photoURI: Uri = FileProvider.getUriForFile(requireActivity(),
                "com.hunorszondi.android.fileprovider",
                photoFile)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    /**
     * Creates an empty image file
     *
     * @param fileName base name of the file
     * @return empty image file
     */
    @Throws(IOException::class)
    private fun createImageFile(fileName: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = fileName + "_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        currentPhotoPath = image.absolutePath
        return image
    }

    /**
     * Creates an image file, if the source of the image is the gallery
     *
     * @param imageFromGallery bitmap image
     */
    private fun createImageFileFromGallery(imageFromGallery: Bitmap) {
        val file: File = createImageFile("profile_picture")

        val fileOut = FileOutputStream(file)
        imageFromGallery.compress(Bitmap.CompressFormat.JPEG, 80, fileOut)
        fileOut.flush()
        fileOut.close()
    }

    /**
     * Checks permission
     *
     * @param permissionType permission type String
     * @return boolean
     */
    private fun checkPermission(permissionType: String): Boolean {
        val permission = ContextCompat.checkSelfPermission(requireContext(), permissionType)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Makes permission request
     *
     * @param permissionTypes permission types Array of Strings
     * @param callbackSuccess function to call if permission is granted
     */
    private fun makePermissionRequest(
        permissionTypes: Array<String>,
        callbackSuccess: (()->Unit)
    ) {
        functionToCallAfterPermission = callbackSuccess
        requestPermissions(permissionTypes, REQUEST_PERMISSION)
    }
}
