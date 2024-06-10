package com.example.abilitytest.activity

import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.abilitytest.FILEPATH
import com.example.abilitytest.R
import com.example.abilitytest.databinding.LearnActivityBinding
import com.example.abilitytest.fragment.EncyclopediaCard
import com.example.abilitytest.utils.MessageUtil
import kotlinx.serialization.json.Json

class LearnActivity: AppCompatActivity() {
    private lateinit var binding: LearnActivityBinding
    private lateinit var player: ExoPlayer
    private val msgUtil: MessageUtil = MessageUtil(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LearnActivityBinding.inflate(layoutInflater)
        
        setContentView(binding.root)

        intent.getStringExtra("knowledge")?.also {
            val card = Json.decodeFromString(EncyclopediaCard.serializer(), it)
            card.video?.also {video ->
                binding.image.visibility = ImageView.GONE
                // 初始化 ExoPlayer
                player = ExoPlayer.Builder(this).build().apply {
                    binding.videoView.player = this
                    setMediaItem(MediaItem.fromUri(FILEPATH.VIDEO_URI + video))
                    // 准备完毕开始播放
                    addListener(object : Player.Listener {
                        @OptIn(UnstableApi::class)
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                // 当视频播放准备完成时开始播放
                                player.play()
                                binding.videoView.hideController()
                            }
                        }
                    })
                    prepare()
                }
            } ?: run {
                binding.videoView.visibility = PlayerView.GONE
                binding.image.visibility = ImageView.VISIBLE
                Glide.with(this).load(FILEPATH.IMAGE_URI + card.image).into(binding.image)
            }

            // 放置别的控件
            binding.title.text = card.title
            binding.content.text = card.content
        }
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) {
            player.stop()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }
}