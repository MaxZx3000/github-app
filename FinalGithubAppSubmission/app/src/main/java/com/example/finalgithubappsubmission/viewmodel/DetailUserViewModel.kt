package com.example.finalgithubappsubmission.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalgithubappsubmission.users.DetailUser

class DetailUserViewModel: ViewModel() {
    private var detailUser = MutableLiveData<DetailUser>()
    private var isFavorite = MutableLiveData<Boolean>()
    interface PrepareConnectionData{
        fun prepareInternetData()
    }
    var iPrepareConnectionData: PrepareConnectionData? = null
    fun setDataFromInternet(){
        iPrepareConnectionData?.prepareInternetData()
    }
    fun getUserData(): MutableLiveData<DetailUser>{
        return detailUser
    }
    fun setIsFavoriteValue(isFavorite: Boolean){
        this.isFavorite.value = isFavorite
    }
    fun getFavoriteValue(): Boolean?{
        return isFavorite.value
    }
}