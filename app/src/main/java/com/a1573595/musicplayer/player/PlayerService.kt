package com.a1573595.musicplayer.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.Weak
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_COMPLETE
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_PAUSE
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_PLAY
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_STOP
import com.a1573595.musicplayer.songList.SongListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.FileDescriptor
import java.lang.Exception

class PlayerService : Service(), PropertyChangeListener {
    companion object {
        const val CHANNEL_ID_MUSIC = "app.MUSIC"
        const val CHANNEL_NAME_MUSIC = "Music"
        const val NOTIFICATION_ID_MUSIC = 101

        const val BROADCAST_ID_MUSIC = 201
        const val NOTIFICATION_PREVIOUS = "notification.PREVIOUS"
        const val NOTIFICATION_PLAY = "notification.PLAY"
        const val NOTIFICATION_NEXT = "notification.NEXT"
        const val NOTIFICATION_CANCEL = "notification.CANCEL"

        const val ACTION_FIND_NEW_SONG = "action.FIND_NEW_SONG"
        const val ACTION_NOT_SONG_FOUND = "action.NOT_FOUND"
    }

    private lateinit var smallRemoteView: RemoteViews
    private lateinit var largeRemoteView: RemoteViews
    private lateinit var intentPREVIOUS: PendingIntent
    private lateinit var intentPlay: PendingIntent
    private lateinit var intentNext: PendingIntent
    private lateinit var intentCancel: PendingIntent

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            when (intent?.action) {
                NOTIFICATION_PREVIOUS -> skipToPrevious()
                NOTIFICATION_PLAY -> {
                    if (isPlaying) {
                        pause()
                    } else {
                        play()
                    }
                }
                NOTIFICATION_NEXT -> skipToNext()
                NOTIFICATION_CANCEL -> {
                    pause()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_DETACH)
                    } else {
                        stopForeground(true)
                    }
                    stopSelf()
                }
            }
        }
    }

    private val metaRetriever = MediaMetadataRetriever()
    private val uriExternal: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val mHandler: Handler = Handler(Looper.getMainLooper()) { msg ->
        val id = msg.data.getString("songID")
        val audioUri = Uri.withAppendedPath(uriExternal, id)

        try {
            contentResolver.openFileDescriptor(audioUri, "r")?.use {
                if (addSong(it.fileDescriptor, id!!, getSongTitle(audioUri))) {
                    playerManager.setChangedNotify(ACTION_FIND_NEW_SONG)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        true
    }
    private val audioObserver: AudioObserver = AudioObserver(mHandler)

    private val playerManager: PlayerManager = PlayerManager()

    private val songList: MutableList<Song> = mutableListOf()
    private var playerPosition: Int = 0    // song queue position
    private var isPlaying: Boolean = false // mediaPlayer.isPlaying may take some time update status
    var isRepeat: Boolean = false
    var isRandom: Boolean = false

    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so clients can call public methods
//        val service: PlayerService = this@PlayerService

        val service by Weak {
            this@PlayerService
        }
    }

    private var binder: LocalBinder? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        initRemoteView()

        contentResolver.registerContentObserver(uriExternal, true, audioObserver)
        registerReceiver()
        addPlayerObserver(this)

        binder = LocalBinder()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
    }

    override fun onDestroy() {
        binder = null

        contentResolver.unregisterContentObserver(audioObserver)
        unregisterReceiver(receiver)
        metaRetriever.release()

        deletePlayerObserver(this)
        playerManager.stop()

        super.onDestroy()
    }

    override fun propertyChange(event: PropertyChangeEvent) {
        when (event.propertyName) {
            ACTION_COMPLETE -> {
                playerManager.playerProgress = 0

                when {
                    isRepeat -> play()
                    isRandom -> play((0 until songList.size).random())
                    else -> skipToNext()
                }
            }
            ACTION_PLAY, ACTION_PAUSE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID_MUSIC, createNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                } else {
                    startForeground(NOTIFICATION_ID_MUSIC, createNotification())
                }
            }
            ACTION_STOP -> {
                isPlaying = false
            }
            ACTION_FIND_NEW_SONG -> {
                Toast.makeText(this, getString(R.string.found_new_song), Toast.LENGTH_SHORT).show()
            }
            ACTION_NOT_SONG_FOUND -> {
                Toast.makeText(this, getString(R.string.no_song_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSong(fd: FileDescriptor, id: String, title: String): Boolean {
        try {
            if (!fd.valid()) {
                return false
            }

            metaRetriever.setDataSource(fd)

            val duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val author = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)

            if (duration.isNullOrEmpty()) {
                return false
            }

            val song = Song(
                id, title, artist ?: author ?: getString(R.string.unknown), duration.toLong()
            )

            if (!songList.contains(song)) {
                songList.add(song)
            }
        } catch (e: Exception) {
            Timber.e(e)
            return false
        }
        return true
    }

    suspend fun readSong() = withContext(Dispatchers.IO) {
        if (songList.isNotEmpty()) return@withContext

        contentResolver.query(
            uriExternal, null, null, null, null
        )?.use { cursor ->
            val indexID: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val indexTitle: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {
                val id = cursor.getString(indexID)
                val title = cursor.getString(indexTitle)
                val audioUri = Uri.withAppendedPath(uriExternal, id)

                contentResolver.openFileDescriptor(audioUri, "r")?.use {
                    addSong(it.fileDescriptor, id, title)
                }
            }
        }
    }

    fun addPlayerObserver(listener: PropertyChangeListener) =
        playerManager.addPropertyChangeListener(listener)

    fun deletePlayerObserver(listener: PropertyChangeListener) =
        playerManager.removePropertyChangeListener(listener)

    fun isPlaying(): Boolean = isPlaying

    fun getSongList() = songList.toList()

    fun getSong(): Song? = songList.getOrNull(playerPosition)

    fun getProgress(): Int = playerManager.playerProgress

    fun play(position: Int = playerPosition) {
        isPlaying = true

        // Is different song
        if (position != playerPosition) {
            playerManager.playerProgress = 0
        }

        playerPosition = when {
            songList.size < 1 -> {
                playerManager.setChangedNotify(ACTION_NOT_SONG_FOUND)
                return
            }
            position >= songList.size -> 0
            position < 0 -> songList.lastIndex
            else -> position
        }

        val audioUri = Uri.withAppendedPath(uriExternal, songList[playerPosition].id)

        contentResolver.openFileDescriptor(audioUri, "r")?.use {
            playerManager.play(it.fileDescriptor)
        } ?: kotlin.run {
            songList.removeAt(playerPosition)
            playerManager.setChangedNotify(ACTION_NOT_SONG_FOUND)

            play()
        }
    }

    fun pause() {
        isPlaying = false

        playerManager.pause()
    }

    fun seekTo(progress: Int) {
        if (isPlaying) {
            playerManager.seekTo(progress)
        } else {
            playerManager.playerProgress = progress
            play()
        }
    }

    fun skipToNext() {
        if (!isRandom) {
            play(playerPosition + 1)
        } else {
            play((0 until songList.size).random())
        }
    }

    fun skipToPrevious() {
        if (!isRandom) {
            play(playerPosition - 1)
        } else {
            play((0 until songList.size).random())
        }
    }

    private fun getSongTitle(uri: Uri): String {
        var title: String? = uri.lastPathSegment

        contentResolver.query(
            uri, null, null, null, null
        )?.use {
            if (it.moveToNext()) {
                title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
            }
        }

        return title ?: ""
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val status = NotificationChannel(
                CHANNEL_ID_MUSIC, CHANNEL_NAME_MUSIC, NotificationManager.IMPORTANCE_LOW
            )
            status.description = "Music player"
            nm.createNotificationChannel(status)
        }
    }

    private fun initRemoteView() {
        smallRemoteView = RemoteViews(packageName, R.layout.notification_small)
        largeRemoteView = RemoteViews(packageName, R.layout.notification_large)

        intentPREVIOUS = PendingIntent.getBroadcast(
            this,
            BROADCAST_ID_MUSIC,
            Intent(NOTIFICATION_PREVIOUS).setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        intentPlay = PendingIntent.getBroadcast(
            this,
            BROADCAST_ID_MUSIC,
            Intent(NOTIFICATION_PLAY).setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        intentNext = PendingIntent.getBroadcast(
            this,
            BROADCAST_ID_MUSIC,
            Intent(NOTIFICATION_NEXT).setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        intentCancel = PendingIntent.getBroadcast(
            this,
            BROADCAST_ID_MUSIC,
            Intent(NOTIFICATION_CANCEL).setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(NOTIFICATION_PREVIOUS)
        intentFilter.addAction(NOTIFICATION_PLAY)
        intentFilter.addAction(NOTIFICATION_NEXT)
        intentFilter.addAction(NOTIFICATION_CANCEL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }
    }

    private fun createNotification(): Notification {
        val song = getSong()

        smallRemoteView.setTextViewText(R.id.tv_name, song?.name)
        smallRemoteView.setImageViewResource(
            R.id.img_play, if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )

        largeRemoteView.setTextViewText(R.id.tv_name, song?.name)
        largeRemoteView.setImageViewResource(
            R.id.img_play, if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        largeRemoteView.setOnClickPendingIntent(R.id.img_previous, intentPREVIOUS)
        largeRemoteView.setOnClickPendingIntent(R.id.img_play, intentPlay)
        largeRemoteView.setOnClickPendingIntent(R.id.img_next, intentNext)
        largeRemoteView.setOnClickPendingIntent(R.id.img_cancel, intentCancel)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_MUSIC)
        notificationBuilder.setSmallIcon(R.drawable.ic_music)
//            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.music))
            .setContentTitle(song?.name).setContentText(song?.author)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOnlyAlertOnce(true)
            .setContentIntent(createContentIntent())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallRemoteView)
            .setCustomBigContentView(largeRemoteView)    //show full remoteView
//            .setOngoing(true) // not working when use startForeground()

        return notificationBuilder.build()
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, SongListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        return PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}