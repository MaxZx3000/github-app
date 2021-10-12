package com.example.consumerapp.users.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.consumerapp.users.MainUser
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "user_table")
@Parcelize
class UserEntity(
    @PrimaryKey @ColumnInfo(name="entity_name") val entityName: String,
    @ColumnInfo(name="entity_type") val entityType: String,
    @ColumnInfo(name="entity_link") val entityImageLink: String
): MainUser(entityName, entityType, entityImageLink){
    companion object{
        const val ENTITY_NAME = "entity_name"
        const val ENTITY_TYPE = "entity_type"
        const val ENTITY_LINK = "entity_link"
    }
}