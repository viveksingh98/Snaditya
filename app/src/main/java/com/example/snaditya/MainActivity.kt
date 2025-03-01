package com.example.snaditya

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var restartBtn: Button
    private lateinit var musicToggleBtn: ImageButton
    private var highScore = 0
    private var soundEffectPlayer: MediaPlayer? = null
    private var backgroundMusicPlayer: MediaPlayer? = null
    private var isFirstGame = true
    private var vibrator: Vibrator? = null
    private var isMusicEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views properly
        scoreText = findViewById(R.id.score_text)
        highScoreText = findViewById(R.id.high_score_text)
        restartBtn = findViewById(R.id.restart_btn)
        gameView = findViewById(R.id.game_view)
        musicToggleBtn = findViewById(R.id.music_toggle_btn)

        // Set up vibrator service
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Load music preference
        isMusicEnabled = getPreferences(MODE_PRIVATE).getBoolean("music_enabled", true)
        updateMusicToggleIcon()

        title = "Aditya's Snake Game"

        // Welcome message with Aditya's name
        val welcomeMessage = "Let's go Aditya! Ready to beat your high score?"
        val toast = Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG)
        toast.show()

        // Load high score from preferences
        highScore = getPreferences(MODE_PRIVATE).getInt("high_score", 0)
        updateHighScore()

        // Set up background music
        setupBackgroundMusic()

        // Music toggle button
        musicToggleBtn.setOnClickListener {
            toggleBackgroundMusic()
        }

        // Personalized start dialog for Aditya
        if (isFirstGame) {
            showWelcomeDialog()
        }

        restartBtn.setOnClickListener {
            // Button animation
            it.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

            gameView.resetGame()
            updateScore(0)
            restartBtn.visibility = View.GONE
            gameView.resume()

            // Play a sound effect when game restarts
            playSound(R.raw.game_start)
        }
    }

    private fun showWelcomeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🎮 Welcome Aditya! 🎮")
        builder.setMessage("Ready to play your special Snake game? Swipe to move the snake and collect as many 'A's as you can!")
        builder.setPositiveButton("Let's Play!") { _, _ ->
            gameView.resume()
            isFirstGame = false

            // Play start sound
            playSound(R.raw.game_start)
        }
        builder.setCancelable(false)
        builder.show()
    }

    fun updateScore(score: Int) {
        runOnUiThread {
            scoreText.text = getString(R.string.score_text, score)
        }
    }

    fun showRestartButton() {
        runOnUiThread {
            val currentScore = gameView.score

            // Vibrate on game over
            vibrator?.vibrate(300)

            // Play game over sound
            playSound(R.raw.game_over)

            if(currentScore > highScore) {
                highScore = currentScore
                // Save new high score
                getPreferences(MODE_PRIVATE).edit().putInt("high_score", highScore).apply()
                updateHighScore()

                // Special congratulations for Aditya
                val newRecordMessage = getString(R.string.new_record, highScore)
                Toast.makeText(this, newRecordMessage, Toast.LENGTH_LONG).show()

                // Extra vibration for new high score
                vibrator?.vibrate(longArrayOf(0, 100, 100, 100, 100, 100), -1)
            }

            // Make button visible with animation
            restartBtn.visibility = View.VISIBLE
            restartBtn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left))
        }
    }

    private fun updateHighScore() {
        highScoreText.text = getString(R.string.high_score_text, highScore)
    }

    private fun setupBackgroundMusic() {
        try {
            // Create and prepare background music player
            backgroundMusicPlayer = MediaPlayer.create(this, R.raw.background_music)
            backgroundMusicPlayer?.apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                if (isMusicEnabled) {
                    start()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not load background music", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBackgroundMusic() {
        isMusicEnabled = !isMusicEnabled

        if (isMusicEnabled) {
            backgroundMusicPlayer?.start()
        } else {
            backgroundMusicPlayer?.pause()
        }

        // Save preference
        getPreferences(MODE_PRIVATE).edit().putBoolean("music_enabled", isMusicEnabled).apply()

        // Update icon
        updateMusicToggleIcon()
    }

    private fun updateMusicToggleIcon() {
        val iconResId = if (isMusicEnabled) {
            R.drawable.ic_volume_on
        } else {
            R.drawable.ic_volume_off
        }
        musicToggleBtn.setImageResource(iconResId)
    }

    private fun playSound(resourceId: Int) {
        // Release any previously playing sound effect player
        soundEffectPlayer?.release()

        // Create new media player for sound effect
        soundEffectPlayer = MediaPlayer.create(this, resourceId)
        soundEffectPlayer?.setOnCompletionListener { it.release() }
        soundEffectPlayer?.start()
    }

    override fun onResume() {
        super.onResume()
        if (!isFirstGame) {
            gameView.resume()
        }

        // Resume background music if enabled
        if (isMusicEnabled) {
            backgroundMusicPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()

        // Pause background music
        backgroundMusicPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundEffectPlayer?.release()
        soundEffectPlayer = null

        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }
}