package com.example.reader_epub

import android.util.Log

object Logger {
    private const val TAG = "ReaderLogger"

    fun log(message: String) {
        Log.d(TAG, message)
    }
}