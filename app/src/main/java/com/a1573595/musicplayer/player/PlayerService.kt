package com.a1573595.musicplayer.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
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
                    ServiceCompat.stopForeground(
                        this@PlayerService,
                        ServiceCompat.STOP_FOREGROUND_REMOVE
                    )
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

        if (!isPlaying) {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        binder = null

        contentResolver.unregisterContentObserver(audioObserver)
        unregisterReceiver(receiver)
        metaRetriever.release()

        deletePlayerObserver(this)
        playerManager.release()

        super.onDestroy()
    }

    override fun propertyChange(event: PropertyChangeEvent) {
        when (event.propertyName) {
            ACTION_COMPLETE -> {
                playerManager.playerProgress = 0
                isPlaying = false

                when {
                    isRepeat -> play()
                    else -> skipToNext()
                }
            }

            ACTION_PLAY -> {
                isPlaying = true
                startForegroundNotification()
            }

            ACTION_PAUSE -> {
                isPlaying = false
                startForegroundNotification()
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
        if (songList.isEmpty()) {
            notifyNoSongFound()
            return
        }

        // Is different song
        if (position != playerPosition) {
            playerManager.playerProgress = 0
        }

        playerPosition = when {
            position >= songList.size -> 0
            position < 0 -> songList.lastIndex
            else -> position
        }

        val audioUri = Uri.withAppendedPath(uriExternal, songList[playerPosition].id)

        contentResolver.openFileDescriptor(audioUri, "r")?.use {
            startPlaybackService()

            if (!playerManager.play(it.fileDescriptor)) {
                notifyNoSongFound()
            }
        } ?: kotlin.run {
            songList.removeAt(playerPosition)

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
        if (songList.isEmpty()) {
            notifyNoSongFound()
            return
        }

        play(if (isRandom) songList.indices.random() else playerPosition + 1)
    }

    fun skipToPrevious() {
        if (songList.isEmpty()) {
            notifyNoSongFound()
            return
        }

        play(if (isRandom) songList.indices.random() else playerPosition - 1)
    }

    private fun notifyNoSongFound() {
        isPlaying = false
        playerManager.playerProgress = 0
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        playerManager.setChangedNotify(ACTION_NOT_SONG_FOUND)
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

    private fun getMediaPlaybackForegroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
    }

    private fun startPlaybackService() {
        ContextCompat.startForegroundService(this, Intent(this, PlayerService::class.java))
        startForegroundNotification()
    }

    private fun startForegroundNotification() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID_MUSIC,
            createNotification(),
            getMediaPlaybackForegroundServiceType()
        )
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

        ContextCompat.registerReceiver(
            this,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun createNotification(): Notification {
        val song = getSong()

        smallRemoteView.setTextViewText(R.id.tv_name, song?.name)

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
