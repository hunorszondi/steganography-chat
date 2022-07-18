package com.hunorszondi.letstego.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.hunorszondi.letstego.R
import kotlinx.android.synthetic.main.right_bubble.view.*

class RightBubble : ConstraintLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.right_bubble, this)
        imageViewRightBubble.visibility = View.GONE
    }

    fun setMessage(message: String) {
        textView.text = message
    }

    fun setImage(imageUrl: String?) {
        if (imageUrl != null) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(imageViewRightBubble)
        }
    }
}