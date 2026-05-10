package com.a1573595.musicplayer.ui.base

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.data.player.PlayerService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.beans.PropertyChangeListener

abstract class PlayerBoundActivity<P : BasePresenter<*>> : BaseActivity<P>(), PropertyChangeListener {
    private val requestWriteExternalStorage: Int = 10
    private val requestReadMediaAudio: Int = 11
    private val requestPostNotifications: Int = 12

    private lateinit var player: PlayerService

    private var isBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            val localBinder = binder as PlayerService.LocalBinder

            localBinder.service?.let {
                player = it

                player.addPlayerObserver(this@PlayerBoundActivity)
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

        if (hasAudioPermission() && !isBound) {
            bindPlayerService()
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

        if (grantResults.isEmpty()) return
        when (requestCode) {
            requestWriteExternalStorage, requestReadMediaAudio -> if (grantResults[0] == PERMISSION_GRANTED) {
                bindPlayerService()
                requestNotificationPermissionIfNeeded()
            } else {
                showNeedPermissionDialog()
            }

            requestPostNotifications -> Unit
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(READ_MEDIA_AUDIO)) {
                requestPermission(requestReadMediaAudio, READ_MEDIA_AUDIO)
            } else {
                requestNotificationPermissionIfNeeded()
            }
        } else if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            requestPermission(requestWriteExternalStorage, READ_EXTERNAL_STORAGE)
        }
    }

    private fun hasAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(READ_MEDIA_AUDIO)
        } else {
            hasPermission(READ_EXTERNAL_STORAGE)
        }
    }

    private fun bindPlayerService() {
        if (!isBound) {
            bindService(
                Intent(this, PlayerService::class.java),
                mConnection,
                BIND_AUTO_CREATE
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(POST_NOTIFICATIONS)
        ) {
            requestPermission(requestPostNotifications, POST_NOTIFICATIONS)
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
