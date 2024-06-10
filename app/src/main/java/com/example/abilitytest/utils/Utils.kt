package com.example.abilitytest.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.abilitytest.R
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object Utils {
    /**
     * 将uri保存到指定路径，然后返回完整路径
     * 默认文件名 路径/当前时间-原名称
     * 需要传入callback 函数
     */
    fun copyFile(context: Context,  uri: Uri, path: String, callBack: (String?) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    val baseDir = "${context.getExternalFilesDir(null)}/$path"
                    val directory = File(baseDir)

                    if (!directory.exists()) {
                        directory.mkdirs()
                    }
                    val file = File(directory, "${now()}-${getFileNameFromUri(context, uri)}")
                    try {
                        FileOutputStream(file).use { out ->
                            resource.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        callBack(file.absolutePath)
                    } catch (e: IOException) {
                        MessageUtil(context).createErrorDialog("复制失败: ${e.message}")
                        callBack(null)
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }

    /**
     * 根据uri返回文件名
     * TODO: 没有做异常处理
     */
    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    /**
     * 返回当前时间
     */
    fun now(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun loadFileFromAssets(context: Context,  fileName: String): String? {
        try {
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            return bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            MessageUtil(context).createErrorDialog("${context.getString(R.string.readFileError)}: ${e.message}")
            return null
        }
    }

    fun runAfter(millis: Long, call: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(call, millis)
    }
}