package com.example.favour

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso


class FirebaseHandler : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)


    }
}