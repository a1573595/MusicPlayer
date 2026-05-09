package com.a1573595.musicplayer.data.player

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
import com.a1573595.musicplayer.common.delegate.WeakReferenceDelegate
import com.a1573595.musicplayer.data.song.MediaStoreSongRepository
import com.a1573595.musicplayer.domain.player.PlaybackCoordinator
import com.a1573595.musicplayer.domain.player.PlaybackEngine
import com.a1573595.musicplayer.domain.player.PlaybackQueue
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.domain.song.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
                    if (playbackCoordinator.isPlaying) {
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
                if (playbackCoordinator.addIfAbsent(song)) {
                    playbackEngine.notifyChanged(ACTION_FIND_NEW_SONG)
                }
            }
        }

        true
    }
    private val audioObserver: MediaStoreAudioObserver = MediaStoreAudioObserver(mHandler)

    private val playbackEngine: PlaybackEngine = MediaPlayerPlaybackEngine()
    private val playbackQueue: PlaybackQueue = PlaybackQueue()

    private lateinit var songRepository: SongRepository
    private lateinit var notificationFactory: PlaybackNotificationFactory
    private lateinit var playbackCoordinator: PlaybackCoordinator

    var isRepeat: Boolean
        get() = playbackCoordinator.isRepeat
        set(value) {
            playbackCoordinator.isRepeat = value
        }
    var isRandom: Boolean
        get() = playbackCoordinator.isRandom
        set(value) {
            playbackCoordinator.isRandom = value
        }

    inner class LocalBinder : Binder() {
        val service by WeakReferenceDelegate {
            this@PlayerService
        }
    }

    private var binder: LocalBinder? = null

    override fun onCreate() {
        super.onCreate()

        songRepository = MediaStoreSongRepository(this)
        notificationFactory = PlaybackNotificationFactory(this)
        notificationFactory.createNotificationChannel()
        playbackCoordinator = PlaybackCoordinator(
            queue = playbackQueue,
            engine = playbackEngine,
            audioSource = MediaStorePlaybackAudioSource(contentResolver, uriExternal),
            onPlaybackStart = ::startPlaybackService,
            onPlaybackStateChanged = ::startForegroundNotification,
            onPlaybackUnavailable = ::notifyNoSongFound
        )

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

        if (!playbackCoordinator.isPlaying) {
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
            ACTION_FIND_NEW_SONG -> {
                Toast.makeText(this, getString(R.string.found_new_song), Toast.LENGTH_SHORT).show()
            }

            ACTION_NOT_SONG_FOUND -> {
                Toast.makeText(this, getString(R.string.no_song_found), Toast.LENGTH_SHORT).show()
            }

            else -> {
                playbackCoordinator.onEngineEvent(event.propertyName)
            }
        }
    }

    suspend fun readSong() {
        if (playbackCoordinator.songs().isNotEmpty()) return

        playbackCoordinator.replaceSongs(songRepository.loadSongs())
    }

    fun addPlayerObserver(listener: PropertyChangeListener) =
        playbackEngine.addObserver(listener)

    fun deletePlayerObserver(listener: PropertyChangeListener) =
        playbackEngine.removeObserver(listener)

    fun isPlaying(): Boolean = playbackCoordinator.isPlaying

    fun getSongList() = playbackCoordinator.songs()

    fun getSong(): Song? = playbackCoordinator.currentSong()

    fun getProgress(): Int = playbackCoordinator.progress()

    fun play(position: Int = playbackQueue.currentIndex) = playbackCoordinator.play(position)

    fun pause() = playbackCoordinator.pause()

    fun seekTo(progress: Int) = playbackCoordinator.seekTo(progress)

    fun skipToNext() = playbackCoordinator.skipToNext()

    fun skipToPrevious() = playbackCoordinator.skipToPrevious()

    private fun notifyNoSongFound() {
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
            notificationFactory.create(
                playbackCoordinator.currentSong(),
                playbackCoordinator.isPlaying
            ),
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
