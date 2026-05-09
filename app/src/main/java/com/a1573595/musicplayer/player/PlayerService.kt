package com.a1573595.musicplayer.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.Weak
import com.a1573595.musicplayer.data.song.MediaStoreSongRepository
import com.a1573595.musicplayer.domain.player.PlaybackEngine
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_COMPLETE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PAUSE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PLAY
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_STOP
import com.a1573595.musicplayer.domain.player.PlaybackQueue
import com.a1573595.musicplayer.domain.song.SongRepository
import com.a1573595.musicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
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

    private val uriExternal: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val mHandler: Handler = Handler(Looper.getMainLooper()) { msg ->
        val id = msg.data.getString("songID") ?: return@Handler true

        serviceScope.launch {
            songRepository.findSong(id)?.let { song ->
                if (playbackQueue.addIfAbsent(song)) {
                    playbackEngine.notifyChanged(ACTION_FIND_NEW_SONG)
                }
            }
        }

        true
    }
    private val audioObserver: AudioObserver = AudioObserver(mHandler)

    private val playbackEngine: PlaybackEngine = PlayerManager()
    private val playbackQueue: PlaybackQueue = PlaybackQueue()

    private lateinit var songRepository: SongRepository
    private lateinit var notificationFactory: PlaybackNotificationFactory
    private var isPlaying: Boolean = false // mediaPlayer.isPlaying may take some time update status
    var isRepeat: Boolean
        get() = playbackQueue.isRepeat
        set(value) {
            playbackQueue.isRepeat = value
        }
    var isRandom: Boolean
        get() = playbackQueue.isRandom
        set(value) {
            playbackQueue.isRandom = value
        }

    inner class LocalBinder : Binder() {
        val service by Weak {
            this@PlayerService
        }
    }

    private var binder: LocalBinder? = null

    override fun onCreate() {
        super.onCreate()

        songRepository = MediaStoreSongRepository(this)
        notificationFactory = PlaybackNotificationFactory(this)
        notificationFactory.createNotificationChannel()

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
        serviceScope.cancel()

        deletePlayerObserver(this)
        playbackEngine.release()

        super.onDestroy()
    }

    override fun propertyChange(event: PropertyChangeEvent) {
        when (event.propertyName) {
            ACTION_COMPLETE -> {
                playbackEngine.progress = 0
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

    suspend fun readSong() {
        if (playbackQueue.songs().isNotEmpty()) return

        playbackQueue.replaceSongs(songRepository.loadSongs())
    }

    fun addPlayerObserver(listener: PropertyChangeListener) =
        playbackEngine.addObserver(listener)

    fun deletePlayerObserver(listener: PropertyChangeListener) =
        playbackEngine.removeObserver(listener)

    fun isPlaying(): Boolean = isPlaying

    fun getSongList() = playbackQueue.songs()

    fun getSong(): Song? = playbackQueue.currentSong()

    fun getProgress(): Int = playbackEngine.progress

    fun play(position: Int = playbackQueue.currentIndex) {
        val previousIndex = playbackQueue.currentIndex
        val song = playbackQueue.play(position)
        if (song == null) {
            notifyNoSongFound()
            return
        }

        if (position != previousIndex) {
            playbackEngine.progress = 0
        }

        playCurrentSong(song)
    }

    private fun playCurrentSong(song: Song) {
        val audioUri = Uri.withAppendedPath(uriExternal, song.id)
        val descriptor = try {
            contentResolver.openFileDescriptor(audioUri, "r")
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

        descriptor?.use {
            startPlaybackService()

            if (!playbackEngine.play(it.fileDescriptor)) {
                notifyNoSongFound()
            }
        } ?: run {
            playbackQueue.removeCurrent()

            play()
        }
    }

    fun pause() {
        isPlaying = false

        playbackEngine.pause()
    }

    fun seekTo(progress: Int) {
        if (isPlaying) {
            playbackEngine.seekTo(progress)
        } else {
            playbackEngine.progress = progress
            play()
        }
    }

    fun skipToNext() {
        val previousIndex = playbackQueue.currentIndex
        val song = playbackQueue.next()
        if (song == null) {
            notifyNoSongFound()
            return
        }

        if (playbackQueue.currentIndex != previousIndex) {
            playbackEngine.progress = 0
        }
        playCurrentSong(song)
    }

    fun skipToPrevious() {
        val previousIndex = playbackQueue.currentIndex
        val song = playbackQueue.previous()
        if (song == null) {
            notifyNoSongFound()
            return
        }

        if (playbackQueue.currentIndex != previousIndex) {
            playbackEngine.progress = 0
        }
        playCurrentSong(song)
    }

    private fun notifyNoSongFound() {
        isPlaying = false
        playbackEngine.progress = 0
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        playbackEngine.notifyChanged(ACTION_NOT_SONG_FOUND)
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
            notificationFactory.create(getSong(), isPlaying),
            getMediaPlaybackForegroundServiceType()
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

}
