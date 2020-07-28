package com.example.favour

import android.content.Context
import android.content.SharedPreferences

class Session() {
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


    lateinit var context: Context


    val PREF_NAME = "SESSION"
    var PRIVATE_MODE = 0

    val IS_LOGIN = "IsLoggedIn"
    val IS_MOBILE_VERIFIED = "IsMobileVerified"

    val NAME = "Name"
    val EMAIL = "Email"
    val ADDRESS = "Address"
    val MOBILE = "Mobile_Number"
    val GENDER = "GENDER"


    constructor(context: Context) : this() {
        this.context = context

        pref = context.getSharedPreferences(
            PREF_NAME,
            PRIVATE_MODE
        )
        editor = pref.edit()
    }

    fun setUsername(username: String?) {
        editor.putString(NAME, username).apply()
    }

    fun getUsername(): String? {
        return pref.getString(NAME, "")
    }

    fun setEmail(email: String?) {
        editor.putString(EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return pref.getString(EMAIL, "Add Email")
    }

    fun setGender(gender: String) {
        editor.putString(GENDER, gender).apply()
    }

    fun getGender(): String? {
        return pref.getString(GENDER, "Add Gender")
    }

    fun setAddress(address: String?) {
        editor.putString(ADDRESS, address).apply()
    }

    fun getAddress(): String? {
        return pref.getString(ADDRESS, "Add Address")
    }

    fun setMobile(address: String?) {
        editor.putString(MOBILE, address).apply()
    }

    fun getMobile(): String? {
        return pref.getString(MOBILE, "Add Mobile")
    }

    fun setLoginState(b: Boolean) {
        editor.putBoolean(IS_LOGIN, b)
    }

    fun getLoginState(): Boolean? {
        return pref.getBoolean(IS_LOGIN, false)
    }

    fun setVerifiedState(b: Boolean) {
        editor.putBoolean(IS_MOBILE_VERIFIED, b).apply()
    }

    fun getVerifiedState(): Boolean? {
        return pref.getBoolean(IS_MOBILE_VERIFIED,false)
    }


}