package com.example.consumerapp.adapters.people

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.consumerapp.users.MainUser
import kotlinx.android.synthetic.main.list_main_item_layout.view.*

open class MainUserAdapter(userData: ArrayList<MainUser>, layoutResourceID: Int) : PeopleAbstractAdapter(userData, layoutResourceID) {
    interface IClickBehaviour{
        fun onClick(position: Int)
    }
    open var iClickBehaviour: IClickBehaviour? = null
    override fun bind(itemView: View, mainUserData: MainUser, position: Int) {
        with(itemView){
            tv_username.text = mainUserData.username
            txt_type.text = mainUserData.type
            Glide.with(itemView.context).load(mainUserData.imageURL).apply(RequestOptions().override(150, 150)).into(image_profile_photo)
        }
        itemView.card_profile_container.setOnClickListener {
            iClickBehaviour?.onClick(position)
        }
    }
}