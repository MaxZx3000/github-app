package com.example.finalgithubappsubmission.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalgithubappsubmission.users.MainUser

class MainUsersViewModel: ViewModel(){
    private val listMainUserData = MutableLiveData<ArrayList<MainUser>>()
    interface PrepareConnectionData{
        fun prepareInternetData()
    }
    var iPrepareConnectionData: PrepareConnectionData? = null
    fun setDataFromInternet(){
        iPrepareConnectionData?.prepareInternetData()
    }
    fun getUserData(): MutableLiveData<ArrayList<MainUser>>{
        return listMainUserData
    }
}