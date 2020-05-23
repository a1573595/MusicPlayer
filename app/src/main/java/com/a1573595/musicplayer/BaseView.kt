package com.a1573595.musicplayer

import android.content.Context
import android.view.View

interface BaseView {
    fun isActive(): Boolean

    fun context(): Context

    fun showToast(msg: String)

    fun showSnackBar(v: View, msg: String)
}