package com.a1573595.musicplayer

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.IBinder
import com.a1573595.musicplayer.player.PlayerManager
import com.a1573595.musicplayer.player.PlayerService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

abstract class BaseSongActivity<P : BasePresenter<*>> : BaseActivity<P>(), Observer {
    private val REQUEST_READ_EXTERNAL_STORAGE: Int = 10
    private lateinit var player: PlayerManager

    private var isBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            val localBinder = binder as PlayerService.LocalBinder
            val service = localBinder.service
            player = service.getPlayerManager()
            player.addObserver(this@BaseSongActivity)
            isBound = true
            playerBound(player)
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

        if (hasPermission(READ_EXTERNAL_STORAGE) && !isBound) {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onRestart() {
        super.onRestart()

        if (isBound) {
            player.addObserver(this)
            updateState()
        }
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            player.deleteObserver(this)
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
            REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults.isNotEmpty() &&
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
        if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            requestPermission(REQUEST_READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        }
    }

    private fun showNeedPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.AnimationDialog)
            .setTitle(getString(R.string.permission_requirement))
            .setMessage(getString(R.string.need_permission_to_access))
            .setPositiveButton(getString(R.string.agree)) { dialog, p1 ->
                dialog.dismiss()
                checkPermission()
            }
            .setNegativeButton(getString(R.string.disagree)) { dialog, p1 ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    abstract fun playerBound(player: PlayerManager)

    abstract fun updateState()
}