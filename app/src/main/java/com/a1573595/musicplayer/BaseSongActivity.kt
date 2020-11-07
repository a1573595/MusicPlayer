package com.a1573595.musicplayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.IBinder
import com.a1573595.musicplayer.player.PlayerService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

abstract class BaseSongActivity<P : BasePresenter<*>> : BaseActivity<P>(), Observer {
    private val REQUEST_WRITE_EXTERNAL_STORAGE: Int = 10

    private lateinit var player: PlayerService

    private var isBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            val localBinder = binder as PlayerService.LocalBinder
            player = localBinder.service

            player.addPlayerObserver(this@BaseSongActivity)
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

        if (hasPermission(WRITE_EXTERNAL_STORAGE) && !isBound) {
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
            REQUEST_WRITE_EXTERNAL_STORAGE -> if (grantResults.isNotEmpty() &&
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
        if (!hasPermission(WRITE_EXTERNAL_STORAGE)) {
            requestPermission(REQUEST_WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun showNeedPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.AnimationDialog)
            .setTitle(getString(R.string.permission_requirement))
            .setMessage(getString(R.string.need_permission_to_access))
            .setPositiveButton(getString(R.string.agree)) { dialog, _ ->
                dialog.dismiss()
                checkPermission()
            }
            .setNegativeButton(getString(R.string.disagree)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    abstract fun playerBound(player: PlayerService)

    abstract fun updateState()
}