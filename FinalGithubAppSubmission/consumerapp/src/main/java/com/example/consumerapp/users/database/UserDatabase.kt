package com.example.consumerapp.users.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class UserDatabase: RoomDatabase(){
    abstract fun userDAO(): UserDAO
    companion object{
        @Volatile
        private var INSTANCE: UserDatabase? = null
        private const val USER_DATABASE_NAME = "user_database"
        fun getDatabase(context: Context): UserDatabase {
            if (INSTANCE != null){
                return INSTANCE as UserDatabase
            }
            synchronized(this){
                val newInstance = Room.databaseBuilder(context, UserDatabase::class.java, USER_DATABASE_NAME)
                INSTANCE = newInstance.build()
                return INSTANCE as UserDatabase
            }
        }
    }
}