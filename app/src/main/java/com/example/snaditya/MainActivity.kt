package com.example.snaditya

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

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

        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("CRASH", "Uncaught exception: ${exception.message}", exception)
        }

        try {
            Log.d(TAG, "Starting MainActivity onCreate")
            setContentView(R.layout.activity_main)

            // Initialize views immediately
            initializeViews()

            // Set up vibrator service
            try {
                vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            } catch (e: Exception) {
                Log.e(TAG, "Could not get vibrator service: ${e.message}")
            }

            // Load music preference
            isMusicEnabled = getPreferences(MODE_PRIVATE).getBoolean("music_enabled", true)
            updateMusicToggleIcon()

            title = "Aditya's Snake Game"

            // Welcome message with Aditya's name
            showWelcomeToast()

            // Load high score from preferences
            highScore = getPreferences(MODE_PRIVATE).getInt("high_score", 0)
            updateHighScore()

            // Set up background music
            setupBackgroundMusic()

            // Music toggle button
            musicToggleBtn.setOnClickListener {
                toggleBackgroundMusic()
            }

            // Restart button click
            setupRestartButton()

            // Set a short delay before showing the welcome dialog
            Handler(Looper.getMainLooper()).postDelayed({
                if (isFirstGame) {
                    showWelcomeDialog()
                } else {
                    gameView.resume()
                }
            }, 500) // 500ms delay

            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate: ${e.message}")
            Toast.makeText(this, "Error starting game", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            scoreText = findViewById(R.id.score_text)
            highScoreText = findViewById(R.id.high_score_text)
            restartBtn = findViewById(R.id.restart_btn)
            gameView = findViewById(R.id.game_view)
            musicToggleBtn = findViewById(R.id.music_toggle_btn)

            // Ensure GameView is visible
            gameView.visibility = View.VISIBLE

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize views: ${e.message}")
            throw e
        }
    }

    private fun showWelcomeToast() {
        try {
            val welcomeMessage = "Let's go Aditya! Ready to beat your high score?"
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing welcome toast: ${e.message}")
        }
    }

    private fun setupRestartButton() {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up restart button: ${e.message}")
        }
    }

    private fun showWelcomeDialog() {
        try {
            runOnUiThread {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("🎮 Welcome Aditya! 🎮")
                builder.setMessage("Ready to play your special Snake game? Swipe to move the snake and collect as many 'A's as you can!")
                builder.setPositiveButton("Let's Play!") { _, _ ->
                    gameView.resume()
                    isFirstGame = false
                    playSound(R.raw.game_start)
                }
                builder.setCancelable(false)

                // Force dialog to show immediately
                val dialog = builder.create()
                dialog.show()

                // Log that dialog is shown
                Log.d(TAG, "Welcome dialog shown")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing welcome dialog: ${e.message}")
            // Continue without dialog if there's an error
            gameView.resume()
            isFirstGame = false
        }
    }

    fun updateScore(score: Int) {
        try {
            runOnUiThread {
                try {
                    scoreText.text = getString(R.string.score_text, score)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating score text: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating score: ${e.message}")
        }
    }

    fun showRestartButton() {
        try {
            runOnUiThread {
                try {
                    val currentScore = gameView.score

                    // Vibrate on game over
                    try {
                        vibrator?.vibrate(300)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error vibrating: ${e.message}")
                    }

                    // Play game over sound
                    playSound(R.raw.game_over)

                    if(currentScore > highScore) {
                        highScore = currentScore
                        // Save new high score
                        getPreferences(MODE_PRIVATE).edit().putInt("high_score", highScore).apply()
                        updateHighScore()

                        // Special congratulations for Aditya
                        try {
                            val newRecordMessage = getString(R.string.new_record, highScore)
                            Toast.makeText(this, newRecordMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error showing high score toast: ${e.message}")
                        }

                        // Extra vibration for new high score
                        try {
                            vibrator?.vibrate(longArrayOf(0, 100, 100, 100, 100, 100), -1)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error with pattern vibration: ${e.message}")
                        }
                    }

                    // Make button visible with animation
                    restartBtn.visibility = View.VISIBLE
                    try {
                        restartBtn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error with button animation: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in showRestartButton runOnUiThread: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing restart button: ${e.message}")
        }
    }

    private fun updateHighScore() {
        try {
            highScoreText.text = getString(R.string.high_score_text, highScore)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating high score: ${e.message}")
        }
    }

    private fun setupBackgroundMusic() {
        try {
            // Check if resource exists
            val resourceId = R.raw.background_music

            try {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

                backgroundMusicPlayer = try {
                    MediaPlayer.create(this, resourceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating MediaPlayer: ${e.message}")
                    null
                }

                backgroundMusicPlayer?.apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f)

                    // Use simpler audio setup that's more compatible
                    setAudioStreamType(AudioManager.STREAM_MUSIC)

                    if (isMusicEnabled) {
                        try {
                            start()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error starting background music: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up background music: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in setupBackgroundMusic: ${e.message}")
        }
    }

    private fun toggleBackgroundMusic() {
        try {
            isMusicEnabled = !isMusicEnabled

            if (isMusicEnabled) {
                try {
                    backgroundMusicPlayer?.start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting music: ${e.message}")
                }
            } else {
                try {
                    backgroundMusicPlayer?.pause()
                } catch (e: Exception) {
                    Log.e(TAG, "Error pausing music: ${e.message}")
                }
            }

            // Save preference
            getPreferences(MODE_PRIVATE).edit().putBoolean("music_enabled", isMusicEnabled).apply()

            // Update icon
            updateMusicToggleIcon()
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling background music: ${e.message}")
        }
    }

    private fun updateMusicToggleIcon() {
        try {
            val iconResId = if (isMusicEnabled) {
                R.drawable.ic_volume_on
            } else {
                R.drawable.ic_volume_off
            }
            musicToggleBtn.setImageResource(iconResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating music toggle icon: ${e.message}")
        }
    }

    private fun playSound(resourceId: Int) {
        try {
            // Release any previously playing sound effect player
            try {
                soundEffectPlayer?.release()
                soundEffectPlayer = null
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing previous sound player: ${e.message}")
            }

            // Create new media player for sound effect
            try {
                soundEffectPlayer = MediaPlayer.create(this, resourceId)
                if (soundEffectPlayer != null) {
                    soundEffectPlayer?.setOnCompletionListener {
                        try {
                            it.release()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error releasing sound player on completion: ${e.message}")
                        }
                    }
                    soundEffectPlayer?.start()
                } else {
                    Log.w(TAG, "Failed to create MediaPlayer for sound $resourceId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound $resourceId: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in playSound method: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (!isFirstGame) {
                gameView.resume()
            }

            // Resume background music if enabled
            if (isMusicEnabled) {
                try {
                    backgroundMusicPlayer?.start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error resuming background music: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            gameView.pause()

            // Pause background music
            try {
                backgroundMusicPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing background music: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            try {
                soundEffectPlayer?.release()
                soundEffectPlayer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing sound player: ${e.message}")
            }

            try {
                backgroundMusicPlayer?.release()
                backgroundMusicPlayer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing background music player: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
    }
}