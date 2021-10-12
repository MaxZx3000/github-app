package com.example.finalgithubappsubmission.viewmodel

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalgithubappsubmission.contentprovider.MappingHelper
import com.example.finalgithubappsubmission.users.database.UserEntity

class FavoriteViewModel: ViewModel() {
    private var favoriteUsersLiveData = MutableLiveData<ArrayList<UserEntity>>()
    private var sortOrder = MutableLiveData<String>()
    fun getFavoriteUsers(): LiveData<ArrayList<UserEntity>>{
        return favoriteUsersLiveData
    }

    fun applySortOrder(sortOrder: String){
        if (sortOrder != this.sortOrder.value){
            this.sortOrder.postValue(sortOrder)
        }
    }
    fun getSortOrder(): LiveData<String>{
        return sortOrder
    }

    fun convertCursorToArrayListLiveData(cursor: Cursor?){
        favoriteUsersLiveData.postValue(MappingHelper.convertCursorToArrayList(cursor))
    }
}