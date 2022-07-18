package com.hunorszondi.letstego.ui.chat

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.IStegoCallback
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.SteganoData
import com.hunorszondi.letstego.utils.stegoUtils.encodeDecode.TextDecodingAsync
import com.hunorszondi.letstego.utils.ResourceUtil
import kotlinx.android.synthetic.main.fragment_decode.*
import kotlin.properties.Delegates
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.viewModels.ChatViewModel

/**
 * UI for displaying images and their hidden messages
 */
class ImageViewerFragment : BaseFragment(), IStegoCallback {

    private var decodeUIActive: Boolean = false
    private var originalPicture: Bitmap? = null
    private lateinit var viewModel: ChatViewModel

    private lateinit var imageUrl: String

    private var hasDecodedText: Boolean by Delegates.observable(false) {
            _, _, newValue ->
        if(newValue) {
            decodedTextView.visibility = View.VISIBLE
        } else {
            decodedTextView.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val IMAGE_URL_ARG_KEY: String = "IMAGE_URL_ARG_KEY"
        fun newInstance() = ImageViewerFragment()
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
        imageUrl = arguments?.getString(IMAGE_URL_ARG_KEY)!!
        initView()
    }

    /**
     * Initializing view
     */
    private fun initView() {
        hintTextView.visibility = View.GONE
        hideDecodeUI()

        secretButton.setOnClickListener { toggleDecodeUI() }
        closeButton.setOnClickListener { requireActivity().onBackPressed() }
        buttonDecode.setOnClickListener {
            if(originalPicture != null) {
                extractTextFromPicture()
            } else {
                Toast.makeText(context, "No image loaded", Toast.LENGTH_SHORT).show()
            }
        }

        loadImage()
    }

    /**
     * Loading image from local file or remote url
     */
    private fun loadImage() {
        if(imageUrl.contains(Regex("^(http|https)://"))) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        originalPicture = resource
                        originalImageView.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) { }
                })
        } else {
            originalPicture = BitmapFactory.decodeFile(imageUrl)
            originalImageView.setImageBitmap(originalPicture)
        }
    }

    /**
     * Toggles the visibility of the image decoding UI
     * PS. Just to have some magical functionality ;)
     */
    private fun toggleDecodeUI() {
        if(decodeUIActive){
            decodeUIActive = false
            hideDecodeUI()
        } else {
            decodeUIActive = true
            showDecodeUI()
        }
    }

    /**
     * Shows decoding UI
     */
    private fun showDecodeUI(){
        decodedTextView.visibility = View.GONE
        inputSecretKey.visibility = View.VISIBLE
        buttonDecode.visibility = View.VISIBLE
    }

    /**
     * Hides decoding UI
     */
    private fun hideDecodeUI(){
        decodedTextView.visibility = View.GONE
        inputSecretKey.visibility = View.GONE
        buttonDecode.visibility = View.GONE
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
        cancelLoading()
    }

    /**
     * Extracts secret information from the chosen image
     */
    private fun extractTextFromPicture() {
        if (!inputSecretKey.text.isEmpty() &&
            originalPicture != null) {
            showLoading()
            val imageSteganography = SteganoData(inputSecretKey.text.toString(),
                originalPicture!!)
            val textDecoding = TextDecodingAsync(this)
            textDecoding.execute(imageSteganography)
        }
    }
}