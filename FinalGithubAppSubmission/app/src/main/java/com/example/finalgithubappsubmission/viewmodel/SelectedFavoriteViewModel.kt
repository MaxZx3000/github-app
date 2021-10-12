package com.example.finalgithubappsubmission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectedFavoriteViewModel: ViewModel(){
    private var isActionMode = MutableLiveData<Boolean>()
    private var selectedFavoriteUsersLiveData = MutableLiveData<ArrayList<String>>()
    fun getIsActionMode(): LiveData<Boolean> {
        return isActionMode
    }
    fun getFavoriteSelectedUsersValue(): ArrayList<String>?{
        if (selectedFavoriteUsersLiveData.value == null){
            selectedFavoriteUsersLiveData.value = ArrayList()
        }
        return selectedFavoriteUsersLiveData.value
    }
    fun setIsActionMode(isActionMode: Boolean){
        this.isActionMode.postValue(isActionMode)
    }
    fun addFavoriteUsersLiveData(username: String){
        selectedFavoriteUsersLiveData.value?.add(username)
    }
    fun removeFavoriteUsersLiveData(username: String){
        selectedFavoriteUsersLiveData.value?.remove(username)
    }
    fun clearFavoriteUsersData(){
        selectedFavoriteUsersLiveData.value?.clear()
    }
}