package com.hunorszondi.letstego.ui.chat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.model.apiModels.MessageModel
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.ui.chat.utils.ChatRecyclerViewAdapter
import com.hunorszondi.letstego.viewModels.ChatViewModel
import kotlinx.android.synthetic.main.fragment_chat.*

/**
 * UI for chatting
 */
class ChatFragment : BaseFragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var messageListAdapter: ChatRecyclerViewAdapter

    private val messageListObserver = Observer<MutableList<MessageModel>> { list ->
        val listToShow = list?: mutableListOf()
        if(listToShow.isEmpty()) {
            emptyListTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else{
            updateMessagesList(listToShow)
            emptyListTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private val imageObserver = Observer<String> { path ->
        if(path != null){
            imageContainer.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(path)
                .placeholder(R.drawable.loading)
                .into(encodedImageToSendImageView)
        } else {
            imageContainer.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ChatViewModel::class.java)
        initView()
        initRecyclerView()

        viewModel.messages.observe(this, messageListObserver)
        viewModel.selectedThumbnailToSend.observe(this, imageObserver)
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        viewModel.fetchMessages(::defaultCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.exitConversation()
        viewModel.messages.postValue(null)
    }

    /**
     * Initializing views
     */
    private fun initView() {
        if(viewModel.selectedThumbnailToSend.value != null) {
            imageContainer.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(viewModel.selectedThumbnailToSend.value)
                .placeholder(R.drawable.loading)
                .into(encodedImageToSendImageView)
        }

        navigationHeader.setTitleVisibility(View.VISIBLE)
            .setTitle(viewModel.currentContact!!.details!!.displayName)
            .setButtonVisibility(View.VISIBLE)
            .setButtonIcon(R.drawable.ic_arrow_back)
            .setButtonClickListener(View.OnClickListener { requireActivity().onBackPressed() })

        sendButton.setOnClickListener {
            val messageToSend = messageInput.text.toString()
            if(messageToSend.isEmpty() && viewModel.selectedThumbnailToSend.value == null) {
                Toast.makeText(context, "Empty message", Toast.LENGTH_SHORT).show()
            } else {
                messageInput.setText("")
                viewModel.sendMessage(messageToSend, ::defaultCallback)
            }
        }

        encodedImageToSendImageView.setOnClickListener {
            if(viewModel.selectedEncodedImageToSend.value != null) {
                openImage(viewModel.selectedEncodedImageToSend.value!!)
            }
        }

        deleteImageButton.setOnClickListener {
            viewModel.selectedEncodedImageToSend.postValue(null)
            viewModel.selectedThumbnailToSend.postValue(null)
        }

        attachImageButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_chatFragment_to_encodeFragment, null))
    }

    /**
     * Initializing recycler view
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        messageListAdapter = ChatRecyclerViewAdapter(ArrayList(),
            viewModel.currentContact!!.details!!.photo,
            ::openImage,
            :: onNewItemAddedToRecyclerView
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = messageListAdapter
        }
    }

    /**
     * Updating messages
     *
     * @param list to update with
     */
    private fun updateMessagesList(list: MutableList<MessageModel>) {
        messageListAdapter.updateList(list)
    }

    /**
     * Opens ImageViewerFragment
     *
     * @param imageUrl image to be shown in ImageViewerFragment
     */
    private fun openImage(imageUrl: String) {
        val bundle = bundleOf(ImageViewerFragment.IMAGE_URL_ARG_KEY to imageUrl)
        if(view != null){
            Navigation.findNavController(view!!).navigate(R.id.action_chatFragment_to_imageViewerFragment, bundle)
        }
    }

    /**
     * Scrolls the recyclerView with messages to last message
     */
    private fun onNewItemAddedToRecyclerView() {
        try {
            recyclerView.scrollToPosition(messageListAdapter.itemCount - 1)
        } catch (error: Exception) { }
    }

    /**
     * Updates UI after backend api response if needed
     *
     * @param status positive or negative update
     * @param message status description
     */
    private fun defaultCallback(status: Boolean, message: String) {
        requireActivity().runOnUiThread {
            cancelLoading()
            if(!status) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    recyclerView.scrollToPosition(messageListAdapter.itemCount - 1)
                } catch (error: Exception) { }
            }
        }
    }
}
