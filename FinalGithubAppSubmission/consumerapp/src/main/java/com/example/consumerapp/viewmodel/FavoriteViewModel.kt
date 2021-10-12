package com.example.consumerapp.viewmodel

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.consumerapp.users.database.UserEntity

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
        val userEntityArrayList = ArrayList<UserEntity>()
        cursor?.moveToFirst()
        while(cursor?.isAfterLast == false){
            cursor.apply {
                val entityName = getString(getColumnIndexOrThrow(UserEntity.ENTITY_NAME))
                val entityType = getString(getColumnIndexOrThrow(UserEntity.ENTITY_TYPE))
                val entityLink = getString(getColumnIndexOrThrow(UserEntity.ENTITY_LINK))
                userEntityArrayList.add(UserEntity(entityName, entityType, entityLink))
            }
            cursor.moveToNext()
        }
        cursor?.close()
        favoriteUsersLiveData.postValue(userEntityArrayList)
    }
}