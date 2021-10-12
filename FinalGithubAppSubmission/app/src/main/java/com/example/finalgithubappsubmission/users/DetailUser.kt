package com.example.finalgithubappsubmission.users

import kotlinx.android.parcel.Parcelize

@Parcelize
open class DetailUser(
    var mainUser: MainUser,
    var fullName: String,
    var company: String,
    var location: String,
    var blogInformation: String,
    var repository: Int,
    var gists: Int,
    var followers: Int,
    var following: Int
): MainUser(mainUser.username, mainUser.type,  mainUser.imageURL)