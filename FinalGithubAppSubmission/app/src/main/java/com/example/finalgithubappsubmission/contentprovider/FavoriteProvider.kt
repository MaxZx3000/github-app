package com.example.finalgithubappsubmission.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.finalgithubappsubmission.users.database.UserDAO
import com.example.finalgithubappsubmission.users.database.UserDatabase
import com.example.finalgithubappsubmission.users.database.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class FavoriteProvider : ContentProvider() {
    private lateinit var userDAO: UserDAO
    private var uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    companion object{
        const val ENTITY_USERNAME_KEY = "com.example.finalgithubappsubmission.ENTITY_USERNAME_KEY"
        const val ENTITY_TYPE_KEY = "com.example.finalgithubappsubmission.ENTITY_TYPE_KEY"
        const val ENTITY_IMAGEURL_KEY = "com.example.finalgithubappsubmission.ENTITY_IMAGEURL_KEY"
        const val USER_FAVORITES_TABLE_NAME = "user_table"
        const val ALL_USER_FAVORITES = 0
        const val ALL_USER_FAVORITES_ASC = 1
        const val ALL_USER_FAVORITES_DESC = 2
        const val RANDOM_USER_FAVORITE = 3
        const val DELETE_FAVORITE_ID = 4
        const val DELETE_ALL_FAVORITE = 5
        const val AUTHORITY = "com.example.finalgithubappsubmission"
        const val DELETE_KEYWORD = "delete"
        const val DELETE_ALL_KEYWORD = "deleteall"
        fun getAuthorityPath(): Uri{
            return Uri.Builder().scheme("content")
                .authority(AUTHORITY)
                .appendPath(USER_FAVORITES_TABLE_NAME)
                .build()
        }

    }
    init {
        uriMatcher.addURI(AUTHORITY, USER_FAVORITES_TABLE_NAME, ALL_USER_FAVORITES)
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$ALL_USER_FAVORITES_ASC", ALL_USER_FAVORITES_ASC)
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$ALL_USER_FAVORITES_DESC", ALL_USER_FAVORITES_DESC)
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$RANDOM_USER_FAVORITE", RANDOM_USER_FAVORITE)
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$DELETE_KEYWORD/*", DELETE_FAVORITE_ID)
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$DELETE_ALL_KEYWORD", DELETE_ALL_FAVORITE)
    }
    override fun getType(uri: Uri): String? {
        return null
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val contentValues = values as ContentValues
        val userEntity = UserEntity(contentValues.getAsString(ENTITY_USERNAME_KEY), contentValues.getAsString(ENTITY_TYPE_KEY), contentValues.getAsString(ENTITY_IMAGEURL_KEY))
        userDAO.insert(userEntity)
        return uri
    }

    override fun onCreate(): Boolean {
        userDAO = UserDatabase.getDatabase(context as Context).userDAO()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when(uriMatcher.match(uri)){
            ALL_USER_FAVORITES -> {
                userDAO.getCursorAllUsers()
            }
            ALL_USER_FAVORITES_ASC -> {
                userDAO.getCursorAllUsersAscending()
            }
            ALL_USER_FAVORITES_DESC -> {
                userDAO.getCursorAllUsersDescending()
            }
            RANDOM_USER_FAVORITE -> {
                val numberOfUsers = userDAO.getCurrentRows()
                if (numberOfUsers != 0){
                    val random = Random.nextInt(numberOfUsers)
                    userDAO.getSpecificUserIndex(random)
                }
                else null
            }
            else -> null
        }
    }
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        when(uriMatcher.match(uri)){
            DELETE_FAVORITE_ID -> {
                return userDAO.deleteByName(uri.lastPathSegment.toString())
            }
            DELETE_ALL_FAVORITE -> {
                return userDAO.deleteAll()
            }
        }
        return 0
    }
}
