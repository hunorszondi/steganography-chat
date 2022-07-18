package com.hunorszondi.letstego.ui.chat.utils

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.model.apiModels.ContactModel
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.hunorszondi.letstego.utils.ResourceUtil

/**
 * Adapter for contact list
 */
class ContactsRecyclerViewAdapter(
    private var contacts: MutableList<ContactModel>,
    private val contactClicked:  (ContactModel)->Unit
) : RecyclerView.Adapter<ContactsRecyclerViewAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact: ContactModel = contacts[position]
        holder.bind(contact)
        holder.itemView.setOnClickListener { contactClicked(contact) }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateList(contacts: MutableList<ContactModel>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        contacts.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, contacts.size)
    }

    class ContactViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var textViewName: TextView? = null
        private var imageView: ImageView? = null
        private var textViewLastmessage: TextView? = null
        var viewBackground: RelativeLayout? = null
        var viewForeground:ConstraintLayout? = null

        init {
            textViewName = itemView.findViewById(R.id.textViewName)
            textViewLastmessage = itemView.findViewById(R.id.lastMessageTextView)
            imageView = itemView.findViewById(R.id.imageViewRightBubble)
            viewBackground = itemView.findViewById(R.id.view_background)
            viewForeground = itemView.findViewById(R.id.view_foreground)
        }

        fun bind(contact: ContactModel) {
            textViewName!!.text = contact.details!!.displayName
            textViewLastmessage!!.text = contact.lastMessage
            if(contact.hasUnreadMessages!!) {
                textViewName!!.setTypeface(null, Typeface.BOLD)
                textViewName!!.setTextColor(ResourceUtil.instance.getColor(R.color.newMessageColor))
                textViewLastmessage!!.setTypeface(null, Typeface.BOLD)
                textViewLastmessage!!.setTextColor(ResourceUtil.instance.getColor(R.color.newMessageColor))
            } else {
                textViewName!!.setTypeface(null, Typeface.NORMAL)
                textViewName!!.setTextColor(ResourceUtil.instance.getColor(R.color.noNewMessageColor))
                textViewLastmessage!!.setTypeface(null, Typeface.NORMAL)
                textViewLastmessage!!.setTextColor(ResourceUtil.instance.getColor(R.color.noNewMessageColor))
            }
            if( contact.details.photo == null) {
                Glide.with(itemView.context)
                    .load(R.drawable.ic_profile_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView!!)
                return
            }
            Glide.with(itemView.context)
                .load(contact.details.photo)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .fallback(R.drawable.ic_profile_placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView!!)
        }
    }

}
