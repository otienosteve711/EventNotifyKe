package com.sc.eventnotifyke.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object CloudinaryUploader {

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        preset: String = "eventnotify_preset", // your Cloudinary upload preset
        onProgress: (Float) -> Unit = {}
    ): String = suspendCoroutine { continuation ->

        MediaManager.get()
            .upload(imageUri)
            .unsigned(preset)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    onProgress(bytes.toFloat() / totalBytes.toFloat())
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("No URL returned"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resumeWithException(Exception(error?.description))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    continuation.resumeWithException(Exception(error?.description))
                }

            }).dispatch(context)
    }
}