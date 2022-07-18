package com.hunorszondi.letstego.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.utils.ImageRotationUtil
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.IStegoCallback
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.SteganoData
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.TextDecodingAsync
import com.hunorszondi.letstego.utils.ResourceUtil
import com.hunorszondi.letstego.viewModels.ChatViewModel
import kotlinx.android.synthetic.main.fragment_decode.*
import java.lang.Exception
import kotlin.properties.Delegates


/**
 * UI for image decoding
 */
class DecodeFragment : BaseFragment(), IStegoCallback {
    private val REQUEST_SELECT_IMAGE_FROM_GALLERY: Int = 101

    private lateinit var originalPicture: Bitmap
    private lateinit var viewModel: ChatViewModel
    private var hasDecodedText: Boolean by Delegates.observable(false) {
            _, _, newValue ->
        if(newValue) {
            decodedTextView.visibility = View.VISIBLE
            hintTextView.visibility = View.INVISIBLE
        } else {
            decodedTextView.visibility = View.INVISIBLE
            hintTextView.visibility = View.VISIBLE
        }
    }

    private var hasLoadedPicture: Boolean by Delegates.observable(false) {
            _, _, newValue ->
        if(newValue) {
            hintTextView.visibility = View.INVISIBLE
        } else {
            hintTextView.visibility = View.VISIBLE
        }
    }


    companion object {
        fun newInstance() = DecodeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_decode, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ChatViewModel::class.java)
        initView()
    }

    private fun initView() {
        decodedTextView.visibility = View.INVISIBLE
        secretButton.visibility = View.GONE

        closeButton.setOnClickListener { requireActivity().onBackPressed() }
        originalImageView.setOnClickListener { selectImageFromGallery() }
        buttonDecode.setOnClickListener { extractTextFromPicture() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                REQUEST_SELECT_IMAGE_FROM_GALLERY -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val imageUri = data!!.data
                        originalPicture = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                        originalPicture = ImageRotationUtil()
                            .rotateImageIfRequired(requireContext(), originalPicture, imageUri!!, true)
                        hasLoadedPicture = true
                        originalImageView.setImageBitmap(originalPicture)
                    } else {
                        Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (ex: Exception) {
            Log.d("DecodeFragment", ex.toString())
            Toast.makeText(context, "Image can not be loaded", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartProcess() {
        Toast.makeText(context, "Decoding started", Toast.LENGTH_SHORT).show()
    }

    override fun onCompleteProcess(result: SteganoData) {
        Toast.makeText(context, "Decoding ended", Toast.LENGTH_SHORT).show()
        if (!result.isDecoded!!)
            decodedTextView.text = ResourceUtil.instance.getString(R.string.no_message_found)
        else {
            if (!result.isSecretKeyWrong!!) {
                val text = ResourceUtil.instance.getString(R.string.decoded) + " " + result.message!!
                decodedTextView.text = text
            } else {
                decodedTextView.text = ResourceUtil.instance.getString(R.string.wrong_key)
            }
        }
        hasDecodedText = true
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
     * Extracts secret information from the choosen image
     */
    private fun extractTextFromPicture() {
        if (!inputSecretKey.text.isEmpty() && originalPicture != null) {
            val imageSteganography = SteganoData(inputSecretKey.text.toString(),
                originalPicture)
            val textDecoding = TextDecodingAsync(this)
            textDecoding.execute(imageSteganography)
        }
    }
}