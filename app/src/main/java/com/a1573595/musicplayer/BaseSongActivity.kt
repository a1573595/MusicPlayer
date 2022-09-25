package com.a1573595.musicplayer

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import com.a1573595.musicplayer.player.PlayerService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.beans.PropertyChangeListener

abstract class BaseSongActivity<P : BasePresenter<*>> : BaseActivity<P>(), PropertyChangeListener {
    private val REQUEST_WRITE_EXTERNAL_STORAGE: Int = 10
    private val REQUEST_READ_MEDIA_AUDIO: Int = 11

    private lateinit var player: PlayerService

    private var isBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            val localBinder = binder as PlayerService.LocalBinder

            localBinder.service?.let {
                player = it

                player.addPlayerObserver(this@BaseSongActivity)
                isBound = true
                playerBound(player)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, PlayerService::class.java)
        startService(intent)

        if ((hasPermission(READ_MEDIA_AUDIO) || hasPermission(READ_EXTERNAL_STORAGE)) && !isBound) {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onRestart() {
        super.onRestart()

        if (isBound) {
            player.addPlayerObserver(this)
            updateState()
        }
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            player.deletePlayerObserver(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isBound) {
            isBound = false
            unbindService(mConnection)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE, REQUEST_READ_MEDIA_AUDIO -> if (grantResults.isNotEmpty() &&
                grantResults[0] == PERMISSION_GRANTED
            ) {
                val intent = Intent(this, PlayerService::class.java)
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            } else {
                showNeedPermissionDialog()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(READ_MEDIA_AUDIO)) {
                requestPermission(REQUEST_READ_MEDIA_AUDIO, READ_MEDIA_AUDIO)
            }
        } else if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            requestPermission(REQUEST_WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        }
    }

    private fun showNeedPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.AnimationDialog)
            .setTitle(getString(R.string.permission_requirement))
            .setMessage(getString(R.string.need_permission_to_access))
            .setPositiveButton(getString(R.string.agree)) { dialog, _ ->
                dialog.dismiss()
                openAPPSettings()
            }
            .setNegativeButton(getString(R.string.disagree)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun openAPPSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    abstract fun playerBound(player: PlayerService)

    abstract fun updateState()
}