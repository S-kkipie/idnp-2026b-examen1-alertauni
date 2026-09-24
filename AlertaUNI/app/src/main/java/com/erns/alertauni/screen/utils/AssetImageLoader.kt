package com.erns.alertauni.screen.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter

class AssetImageLoader(private val context: Context) {

    fun loadImage(fileName: String): ImageBitmap {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap.asImageBitmap()
    }
    fun loadPainter(fileName: String): Painter {
        val assetManager = context.assets
        assetManager.open(fileName).use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return BitmapPainter(bitmap.asImageBitmap())
        }
    }
}
