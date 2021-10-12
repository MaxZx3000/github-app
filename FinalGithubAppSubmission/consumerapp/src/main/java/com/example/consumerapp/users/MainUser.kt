package com.example.consumerapp.users

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class MainUser(var username: String, var type: String, var imageURL: String): Parcelable