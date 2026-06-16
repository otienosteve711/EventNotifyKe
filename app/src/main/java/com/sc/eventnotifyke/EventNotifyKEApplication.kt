package com.sc.eventnotifyke

import android.app.Application
import com.cloudinary.android.MediaManager

class EventNotifyKEApplication : Application() {
    override fun onCreate() {
        super.onCreate()


        val config = mapOf(
            "cloud_name" to "dbv39qei9",

        )

        MediaManager.init(this, config)
    }
}