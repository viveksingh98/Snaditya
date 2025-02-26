package com.example.snaditya

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var restartBtn: Button
    private var highScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views properly
        scoreText = findViewById(R.id.score_text)
        restartBtn = findViewById(R.id.restart_btn)
        gameView = findViewById(R.id.game_view)

        title = "Aditya's Snake Game"
        Toast.makeText(this, "Let's go Aditya!", Toast.LENGTH_SHORT).show()

        // Load high score from preferences
        highScore = getPreferences(MODE_PRIVATE).getInt("high_score", 0)
        updateHighScore()

        restartBtn.setOnClickListener {
            gameView.resetGame()
            scoreText.text = "Aditya's Score: 0"
            restartBtn.visibility = View.GONE
            gameView.resume()
        }
    }

    fun showRestartButton() {
        runOnUiThread {
            if(gameView.score > highScore) {
                highScore = gameView.score
                // Save new high score
                getPreferences(MODE_PRIVATE).edit().putInt("high_score", highScore).apply()
                updateHighScore()
                Toast.makeText(this, "Wow Aditya! New Record: $highScore", Toast.LENGTH_LONG).show()
            }
            restartBtn.visibility = View.VISIBLE
        }
    }

    private fun updateHighScore() {
        findViewById<TextView>(R.id.high_score_text).text = "High Score: $highScore"
    }
}