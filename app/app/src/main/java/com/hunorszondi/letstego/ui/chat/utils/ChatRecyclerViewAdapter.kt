package com.hunorszondi.letstego.ui.chat.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.model.apiModels.MessageModel
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hunorszondi.letstego.R
import kotlinx.android.synthetic.main.left_bubble.view.*
import kotlinx.android.synthetic.main.right_bubble.view.*
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.hunorszondi.letstego.utils.DateUtil

/**
 * Adapter for chat messages
 */
class ChatRecyclerViewAdapter(private var messages: MutableList<MessageModel>,
                              private var contactPictureUrl: String?,
                              var onImageClickListener: (String)->Unit,
                              var onNewItemAdded: ()->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.right_bubble, parent, false)
                return ChatRightBubbleViewHolder(itemView)
            }
            1 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.left_bubble, parent, false)
                return ChatLeftBubbleViewHolder(itemView)
            }
        }
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.left_bubble, parent, false)
        return ChatLeftBubbleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: MessageModel = messages[position]

        when (holder.itemViewType) {
            0 -> if(holder is ChatRightBubbleViewHolder) {
                holder.bind(message)
                holder.itemView.imageViewRightBubble.setOnClickListener {
                    if(message.photo != null){
                        onImageClickListener(message.photo)
                    }
                }
            }
            1 -> if(holder is ChatLeftBubbleViewHolder) {
                holder.bind(message)
                holder.itemView.imageViewLeftBubble.setOnClickListener {
                    if(message.photo != null){
                        onImageClickListener(message.photo)
                    }
                }
                Glide.with(holder.itemView.context)
                    .load(contactPictureUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.itemView.profileImageView)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        val message: MessageModel = messages[position]
        if (message.authorName == Session.instance.currentUser?.userName) {
            return 0
        }
        return 1
    }

    override fun getItemCount(): Int = messages.size

    fun appendMessage(message: MessageModel) {
        messages.add(message)
        notifyDataSetChanged()
    }

    fun updateList(messages: MutableList<MessageModel>) {
        this.messages = messages
        notifyDataSetChanged()
        onNewItemAdded()
    }

    class ChatRightBubbleViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var textView: TextView? = null
        var imageView: ImageView? = null
        var dateTextVew: TextView? = null

        init {
            textView = itemView.findViewById(R.id.textView)
            imageView = itemView.findViewById(R.id.imageViewRightBubble)
            dateTextVew = itemView.findViewById(R.id.dateTextView)
            imageView!!.visibility = View.GONE
        }

        fun bind(message: MessageModel) {
            if(!message.content.isEmpty()){
                textView?.visibility = View.VISIBLE
                textView?.text = message.content
            } else {
                textView?.visibility = View.GONE
            }
            if (message.photo != null) {
                imageView!!.visibility = View.VISIBLE
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(16))
                Glide.with(itemView.context)
                    .load(message.thumbnail)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.retry)
                    .apply(requestOptions)
                    .into(imageView!!)
            } else {
                imageView!!.visibility = View.GONE
            }
            if(message.date != null && message.date != 0L) {
                dateTextVew!!.text = DateUtil.getElegantDate(message.date)
            } else {
                dateTextVew!!.text = ""
            }

        }
    }

    class ChatLeftBubbleViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var textView: TextView? = null
        var profileImageview: ImageView? = null
        var imageView: ImageView? = null
        var dateTextVew: TextView? = null

        init {
            textView = itemView.findViewById(R.id.textView)
            imageView = itemView.findViewById(R.id.imageViewLeftBubble)
            profileImageview = itemView.findViewById(R.id.profileImageView)
            dateTextVew = itemView.findViewById(R.id.dateTextView)
            imageView!!.visibility = View.GONE
        }

        fun bind(message: MessageModel) {
            if(!message.content.isEmpty()){
                textView?.visibility = View.VISIBLE
                textView?.text = message.content
            } else {
                textView?.visibility = View.GONE
            }

            if (message.photo != null) {
                imageView!!.visibility = View.VISIBLE
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(16))
                Glide.with(itemView.context)
                    .load(message.thumbnail)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.retry)
                    .apply(requestOptions)
                    .into(imageView!!)
            } else {
                imageView!!.visibility = View.GONE
            }
            dateTextVew!!.text = DateUtil.getElegantDate(message.date)
        }
    }
}


