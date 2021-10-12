package com.example.finalgithubappsubmission.adapters.people

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.MainUser
import kotlinx.android.synthetic.main.list_follow_item_layout.view.*

class FollowAdapter(userData: ArrayList<MainUser>, layoutResourceID: Int): PeopleAbstractAdapter(userData, layoutResourceID){
    companion object{
        private const val EVEN_DIVISOR = 2
    }
    interface ButtonClick{
        fun onClickButton(view: View, position: Int)
    }
    var iButtonClick: ButtonClick? = null
    override fun bind(itemView: View, mainUserData: MainUser, position: Int) {
        SettingsComponent.colorizeTaggedViews(itemView.context, itemView.root_view)
        with(itemView){
            tv_username.text = mainUserData.username
            txt_type.text = mainUserData.type
            if (position % EVEN_DIVISOR == 0){
                setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorLightGray))
            }
            Glide.with(itemView.context).load(mainUserData.imageURL).into(itemView.img_people)
            btn_copy_username.setOnClickListener {
                iButtonClick?.onClickButton(itemView, position)
            }
        }
    }
}