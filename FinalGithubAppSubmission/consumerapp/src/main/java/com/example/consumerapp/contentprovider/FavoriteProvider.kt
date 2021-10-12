package com.example.consumerapp.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.consumerapp.users.database.UserDAO
import com.example.consumerapp.users.database.UserDatabase
import com.example.consumerapp.users.database.UserEntity
import kotlinx.coroutines.*

class FavoriteProvider : ContentProvider() {
    private lateinit var userDAO: UserDAO
    private var uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    companion object{
        const val ENTITY_USER_KEY = "com.example.finalgithubappsubmission.ENTITY_USER_KEY"
        const val USER_FAVORITES_TABLE_NAME = "user_table"
        const val ALL_USER_FAVORITES = 0
        const val ALL_USER_FAVORITES_ASC = 1
        const val ALL_USER_FAVORITES_DESC = 2
        const val DELETE_FAVORITE_ID = 3
        const val AUTHORITY = "com.example.finalgithubappsubmission"
        const val DELETE_KEYWORD = "delete"
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
        uriMatcher.addURI(AUTHORITY, "$USER_FAVORITES_TABLE_NAME/$DELETE_KEYWORD/*", DELETE_FAVORITE_ID)
    }
    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        GlobalScope.launch(Dispatchers.IO) {
            userDAO.insert(values?.get(ENTITY_USER_KEY) as UserEntity)
        }
        return null
    }

    override fun onCreate(): Boolean {
        userDAO = UserDatabase.getDatabase(context as Context).userDAO()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        when(uriMatcher.match(uri)){
            ALL_USER_FAVORITES -> {
                cursor = userDAO.getCursorAllUsers()
            }
            ALL_USER_FAVORITES_ASC -> {
                cursor = userDAO.getCursorAllUsersAscending()
            }
            ALL_USER_FAVORITES_DESC -> {
                cursor = userDAO.getCursorAllUsersDescending()
            }
        }
        return cursor
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
        }
        return 0
    }
}
