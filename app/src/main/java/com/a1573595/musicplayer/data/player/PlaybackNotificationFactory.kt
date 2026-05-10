package com.a1573595.musicplayer.data.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.songlist.SongListActivityBase

class PlaybackNotificationFactory(
    private val context: Context
) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            PlayerService.CHANNEL_ID_MUSIC,
            PlayerService.CHANNEL_NAME_MUSIC,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Music player"
        notificationManager.createNotificationChannel(channel)
    }

    fun create(song: Song?, isPlaying: Boolean): Notification {
        val smallRemoteView = RemoteViews(context.packageName, R.layout.notification_small).apply {
            setTextViewText(R.id.tv_name, song?.name)
        }
        val largeRemoteView = RemoteViews(context.packageName, R.layout.notification_large).apply {
            setTextViewText(R.id.tv_name, song?.name)
            setImageViewResource(
                R.id.img_play,
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            setOnClickPendingIntent(R.id.img_previous, controlIntent(PlayerService.NOTIFICATION_PREVIOUS))
            setOnClickPendingIntent(R.id.img_play, controlIntent(PlayerService.NOTIFICATION_PLAY))
            setOnClickPendingIntent(R.id.img_next, controlIntent(PlayerService.NOTIFICATION_NEXT))
            setOnClickPendingIntent(R.id.img_cancel, controlIntent(PlayerService.NOTIFICATION_CANCEL))
        }

        return NotificationCompat.Builder(context, PlayerService.CHANNEL_ID_MUSIC)
            .setSmallIcon(R.drawable.ic_music)
            .setContentTitle(song?.name)
            .setContentText(song?.author)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallRemoteView)
            .setCustomBigContentView(largeRemoteView)
            .build()
    }

    private fun controlIntent(action: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            PlayerService.BROADCAST_ID_MUSIC,
            Intent(action).setPackage(context.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, SongListActivityBase::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
