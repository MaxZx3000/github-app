package com.example.consumerapp.adapters.people

import android.view.*
import com.example.consumerapp.users.MainUser
import kotlinx.android.synthetic.main.list_main_item_layout.view.*
import kotlin.collections.ArrayList

class FavoriteAdapter(favoriteUsers: ArrayList<MainUser>, layoutResourceId: Int): MainUserAdapter(favoriteUsers, layoutResourceId){
    interface ILongClickBehaviour{
        fun longClickBehaviour(checkView: View, mainUserData: MainUser, position: Int)
    }
    interface IClickBehaviour{
        fun onClick(checkView: View, position: Int, mainUserData: MainUser)
    }
    interface IShowCheckBehaviour{
        fun checkBehaviour(checkView: View, mainUserData: MainUser)
    }
    var iLongHoldBehavior: ILongClickBehaviour? = null
    var iFavoriteClickBehaviour: IClickBehaviour? = null
    var iShowCheckBehaviour: IShowCheckBehaviour? = null
    override fun bind(itemView: View, mainUserData: MainUser, position: Int) {
        super.bind(itemView, mainUserData, position)
        iShowCheckBehaviour?.checkBehaviour(itemView.img_check, mainUserData)
        with(itemView.card_profile_container){
            setOnClickListener {
                iFavoriteClickBehaviour?.onClick(img_check, position, mainUserData)
            }
            setOnLongClickListener {
                iLongHoldBehavior?.longClickBehaviour(img_check, mainUserData, position)
                true
            }
        }
    }
}