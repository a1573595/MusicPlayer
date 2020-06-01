package com.a1573595.musicplayer

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<P : BasePresenter<*>> : AppCompatActivity(), BaseView {
    protected lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
    }

    override fun isActive(): Boolean =
        lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) ||
                lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    override fun context(): Context = this

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackBar(v: View, msg: String) {
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show()
    }

    protected fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED

    protected fun requestPermission(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    protected abstract fun createPresenter(): P
}
