package com.hunorszondi.letstego.customViews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.utils.ResourceUtil
import kotlinx.android.synthetic.main.contact_item.view.*

class ContactItem : ConstraintLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.contact_item, this)
        textViewName.text = ResourceUtil.instance.getString(R.string.name)
    }

    fun setContactName(name: String) {
        textViewName.text = name
    }

    fun setImage(imageUrl: String?) {
        if( imageUrl == null) {
            Glide.with(context)
                .load(R.drawable.ic_profile_placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewRightBubble)
            return
        }
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .fallback(R.drawable.ic_profile_placeholder)
            .apply(RequestOptions.circleCropTransform())
            .into(imageViewRightBubble)
    }
}