package com.example.finalgithubappsubmission.adapters.people

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalgithubappsubmission.users.MainUser

abstract class PeopleAbstractAdapter(val arrayDataUser: ArrayList<MainUser>, private val layoutResourceID: Int): RecyclerView.Adapter<PeopleAbstractAdapter.ViewHolder>(){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindViewHolder(position)
    }

    override fun getItemCount(): Int {
        return arrayDataUser.size
    }

    abstract fun bind(itemView: View, mainUserData: MainUser, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResourceID, parent, false)
        return ViewHolder(view)
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindViewHolder(position: Int){
            bind(itemView, arrayDataUser[position], position)
        }
    }
}