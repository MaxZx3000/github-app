package com.example.finalgithubappsubmission.contentprovider

import android.database.Cursor
import com.example.finalgithubappsubmission.users.database.UserEntity

object MappingHelper{
    fun convertCursorToArrayList(cursor: Cursor?): ArrayList<UserEntity>{
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
        return userEntityArrayList
    }
}