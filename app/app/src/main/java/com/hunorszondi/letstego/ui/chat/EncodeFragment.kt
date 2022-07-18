package com.hunorszondi.letstego.ui.chat

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_encode.*
import android.graphics.Bitmap
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import java.io.File
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.hunorszondi.letstego.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.utils.DialogFactory
import com.hunorszondi.letstego.utils.ImageRotationUtil
import com.hunorszondi.letstego.utils.ResourceUtil
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.EncodeDecode
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.SteganoData
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.TextEncodingAsync
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.IStegoCallback
import com.hunorszondi.letstego.viewModels.ChatViewModel
import org.apache.commons.io.FileUtils
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.properties.Delegates

/**
 * UI for encoding image
 */
class EncodeFragment : BaseFragment(), IStegoCallback {
    private val REQUEST_TAKE_PHOTO: Int = 100
    private val REQUEST_SELECT_IMAGE_FROM_GALLERY: Int = 101
    private val REQUEST_PERMISSION: Int = 200

    private var originalPicture: Bitmap? = null
    private lateinit var currentPhotoPath: String
    private lateinit var encodedPicture: Bitmap
    private lateinit var viewModel: ChatViewModel
    private lateinit var functionToCallAfterPermission: (()->Unit)

    private var hasLoadedPicture: Boolean by Delegates.observable(false) {
            _, _, newValue ->
        if(newValue) {
            hintTextView.visibility = View.INVISIBLE
        } else {
            hintTextView.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newInstance() = EncodeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_encode, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ChatViewModel::class.java)
        originalImageView.setOnClickListener { openImageSelector() }
        buttonEncode.setOnClickListener {
            if(originalPicture != null) {
                embedTextInPicture()
            } else {
                Toast.makeText(context, "No image loaded", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                /**
                 * After an image has been loaded from gallery, here will be handled the new data
                 */
                REQUEST_SELECT_IMAGE_FROM_GALLERY -> {
                    if (resultCode == RESULT_OK) {
                        val imageUri = data!!.data
                        originalPicture = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                        originalPicture = ImageRotationUtil()
                            .rotateImageIfRequired(requireContext(), originalPicture!!, imageUri!!, true)
                        hasLoadedPicture = true
                        originalImageView.setImageBitmap(originalPicture)
                    } else {
                        Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
                    }
                }
                /**
                 * After an image has been captured with camera, here will be handled the new data
                 */
                REQUEST_TAKE_PHOTO -> {
                    if (resultCode == RESULT_OK) {
                        val file = File(currentPhotoPath)
                        originalPicture = MediaStore.Images.Media
                            .getBitmap(requireContext().contentResolver, Uri.fromFile(file))
                        originalPicture = ImageRotationUtil()
                            .rotateImageIfRequired(requireContext(),
                                originalPicture!!,
                                Uri.parse(currentPhotoPath),
                                false
                            )
                        hasLoadedPicture = true
                        originalImageView.setImageBitmap(originalPicture)
                    }
                }
            }
        }catch (ex: Exception) {
            Log.d("EncodeFragment", ex.toString())
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
                    functionToCallAfterPermission()
                }
            }
        }
    }

    override fun onStartProcess() {
        Toast.makeText(context, "Encoding started", Toast.LENGTH_SHORT).show()
    }

    override fun onCompleteProcess(result: SteganoData) {
        encodedPicture = result.encodedImage!!
        originalImageView.setImageBitmap(originalPicture)
        if(!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            makePermissionRequest(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), ::writeEncodedPictureToFile)
        } else {
            writeEncodedPictureToFile()
        }
    }

    /**
     * Writes the encoded image into a file
     */
    private fun writeEncodedPictureToFile() {
        createStorageDirectories()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(Date())
        val encodedFile = File(
            "${Environment.getExternalStorageDirectory()}/letstego",
            "letstego_$timeStamp.png"
        )
        viewModel.selectedEncodedImageToSend.postValue(encodedFile.absolutePath)
        val fileOutEncoded = FileOutputStream(encodedFile)
        encodedPicture.compress(Bitmap.CompressFormat.PNG, 100, fileOutEncoded)
        fileOutEncoded.flush()
        fileOutEncoded.close()

        val thumbnailFile = File("${Environment.getExternalStorageDirectory()}/letstego", "letstego_$timeStamp.jpg"
        )
        val fileOutThumbnail = FileOutputStream(thumbnailFile)
        viewModel.selectedThumbnailToSend.postValue(thumbnailFile.absolutePath)
        encodedPicture.compress(Bitmap.CompressFormat.JPEG, 20, fileOutThumbnail)
        fileOutThumbnail.flush()
        fileOutThumbnail.close()

        cancelLoading()
        requireActivity().onBackPressed()
    }

    /**
     * Creates a public directory to store the encoded images
     */
    private fun createStorageDirectories() {
        val letstegoDirectory = File(
            Environment.getExternalStorageDirectory(),"letstego")
        if(!letstegoDirectory.exists()) {
            letstegoDirectory.mkdir()
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
            val photoFile: File = createImageFile()
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(Date())
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
     * Creates an empty image file
     *
     * @return empty image file
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        return createImageFile("JPEG")
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

    /**
     * Embeds the secret message in the cover image
     */
    private fun embedTextInPicture() {
        if (!inputTextToHide.text.isEmpty() &&
            !inputSecretKey.text.isEmpty() &&
                originalPicture != null) {

            val message = inputTextToHide.text.toString()
            val secretKey = inputSecretKey.text.toString()
            val numberOfPixelsInImage = originalPicture!!.height * originalPicture!!.width
            val numberOfPixelForMessage = EncodeDecode.numberOfPixelForMessage(message)

            if(numberOfPixelsInImage < numberOfPixelForMessage) {
                Toast.makeText(requireContext(),
                    "Message is too long($numberOfPixelForMessage/$numberOfPixelsInImage)",
                    Toast.LENGTH_SHORT).show()
            }

            showLoading()
            val steganoData = SteganoData(message, secretKey, originalPicture!!)
            val textEncoding = TextEncodingAsync(this)
            textEncoding.execute(steganoData)
        }
    }
}