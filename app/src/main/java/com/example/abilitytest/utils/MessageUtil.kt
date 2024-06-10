package com.example.abilitytest.utils

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import com.example.abilitytest.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.merge

class MessageUtil(
    private val context: Context
) {
    fun createToast(msg: String) {
        Log.i("yuri", "sfsf")
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun createDialog(title: String, msg: String, listener: DialogInterface.OnClickListener? = null) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(msg)
            .setNegativeButton(context.getString(R.string.ok), listener)
            .show()
    }

    fun createErrorDialog(msg: String) {
        createDialog(context.getString(R.string.error), msg)
    }

    fun valueCheck(view: Any?, msg: String): Boolean {
        if (view.toString().isEmpty()) {
            createDialog("Error", msg)
            return false
        }
        return true
    }
}