package com.rtnutils.utils

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object IconUtils {
    fun getAppIconBase64(icon: Drawable): String {
        val bitmap: Bitmap =
                if (icon is BitmapDrawable) {
                    icon.bitmap
                } else {
                    val width = icon.intrinsicWidth
                    val height = icon.intrinsicHeight
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                        val canvas = Canvas(this)
                        icon.setBounds(0, 0, canvas.width, canvas.height)
                        icon.draw(canvas)
                    }
                }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/png;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"
    }
}
