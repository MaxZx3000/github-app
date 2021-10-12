package com.example.finalgithubappsubmission.users.database

import android.database.Cursor
import androidx.room.*

@Dao
interface UserDAO{
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: UserEntity)
    @Query("DELETE FROM user_table WHERE username = :name")
    fun deleteByName(name: String): Int
    @Transaction @Query("SELECT * FROM user_table")
    fun getAllUsers(): List<UserEntity>
    @Transaction @Query ("SELECT * FROM user_table WHERE username = :name")
    fun getSpecificUser(name: String): UserEntity?
    @Transaction @Query ("SELECT * FROM user_table LIMIT 1 OFFSET :index")
    fun getSpecificUserIndex(index: Int): Cursor
    @Transaction @Query("SELECT COUNT(*) FROM user_table")
    fun getCurrentRows(): Int
    @Transaction @Query ("SELECT * FROM user_table")
    fun getCursorAllUsers(): Cursor
    @Transaction @Query ("SELECT * FROM user_table ORDER BY entity_name COLLATE NOCASE ASC")
    fun getCursorAllUsersAscending(): Cursor
    @Transaction @Query ("SELECT * FROM user_table ORDER BY entity_name COLLATE NOCASE DESC")
    fun getCursorAllUsersDescending(): Cursor
    @Query("DELETE FROM user_table")
    fun deleteAll(): Int
}