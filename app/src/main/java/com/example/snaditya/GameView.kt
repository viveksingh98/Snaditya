package com.example.snaditya

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs), Runnable, GestureDetector.OnGestureListener {

    companion object {
        private const val TAG = "GameView"
    }

    // Initialize these properties directly in their declaration
    private val holder: SurfaceHolder = getHolder()
    private val paint: Paint = Paint()
    private val gestureDetector: GestureDetector = GestureDetector(context, this)
    private var gameThread: Thread? = null
    private var playing = false
    var score = 0
    private var snakeGame: SnakeGame = SnakeGame()

    // Special effects for Aditya
    private val specialMessages = arrayOf(
        "Great job Aditya!",
        "Aditya is awesome!",
        "Super snake skills, Aditya!",
        "Aditya the Snake Master!",
        "Keep going, Aditya!"
    )
    private var currentMessage = ""
    private var showSpecialMessage = false
    private var messageTimer = 0
    private val specialColors = arrayOf(
        Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.WHITE
    )
    private var specialColorIndex = 0
    private var specialSnakeTimer = 0
    private var isSpecialSnake = false
    private var surfaceReady = false
    private var isFirstDraw = true

    init {
        try {
            Log.d(TAG, "Initializing GameView")

            // Force visibility to VISIBLE
            visibility = View.VISIBLE

            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d(TAG, "Surface created")
                    surfaceReady = true

                    // Post a message to start the game after surface is created
                    Handler(Looper.getMainLooper()).post {
                        // Initialize with current dimensions
                        if (width > 0 && height > 0) {
                            snakeGame.setScreenSize(width, height)
                        }
                        invalidate()
                    }
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    Log.d(TAG, "Surface changed: $width x $height")
                    if (width > 0 && height > 0) {
                        snakeGame.setScreenSize(width, height)
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    Log.d(TAG, "Surface destroyed")
                    surfaceReady = false
                    pause()
                }
            })

            // Set default dimensions
            snakeGame.setScreenSize(600, 600)

            // Make sure we're visible
            post {
                if (visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                }
                Log.d(TAG, "GameView post-initialization, visibility set to VISIBLE")
            }

            Log.d(TAG, "GameView initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in GameView initialization: ${e.message}")
        }
    }

    private fun isSurfaceReady(): Boolean {
        return holder.surface?.isValid == true && surfaceReady
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        try {
            Log.d(TAG, "onSizeChanged: $w x $h")
            if (w > 0 && h > 0) {
                snakeGame.setScreenSize(w, h)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onSizeChanged: ${e.message}")
        }
    }

    override fun run() {
        try {
            Log.d(TAG, "Game loop started with dimensions: ${width}x${height}, blockSize: ${snakeGame.blockSize}")
            while (playing) {
                if (!isSurfaceReady()) {
                    // Skip frames if surface not ready
                    Log.d(TAG, "Surface not ready, skipping frame")
                    Thread.sleep(100)
                    continue
                }

                update()
                draw()
                control()
            }
            Log.d(TAG, "Game loop ended")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in game loop: ${e.stackTraceToString()}")
            playing = false
        }
    }

    private fun update() {
        try {
            if (!snakeGame.moveSnake()) {
                gameOver()
                return
            }

            if (snakeGame.checkFoodCollision()) {
                score++
                snakeGame.growSnake()
                snakeGame.spawnFood()

                // Special effects for Aditya when scoring
                if (score % 5 == 0) {
                    // Special message every 5 points
                    showSpecialMessage = true
                    messageTimer = 30 // Show for about 3 seconds
                    currentMessage = specialMessages[Random.nextInt(specialMessages.size)]
                }

                // Special snake color effect
                if (score % 3 == 0) {
                    isSpecialSnake = true
                    specialSnakeTimer = 20 // About 2 seconds
                    specialColorIndex = (specialColorIndex + 1) % specialColors.size
                }

                // Update score display in real-time
                try {
                    (context as? MainActivity)?.updateScore(score)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update score in MainActivity: ${e.message}")
                }
            }

            // Update timers for effects
            if (showSpecialMessage && messageTimer > 0) {
                messageTimer--
                if (messageTimer <= 0) {
                    showSpecialMessage = false
                }
            }

            if (isSpecialSnake && specialSnakeTimer > 0) {
                specialSnakeTimer--
                if (specialSnakeTimer <= 0) {
                    isSpecialSnake = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in update: ${e.message}")
        }
    }

    private fun draw() {
        try {
            if (!isSurfaceReady()) {
                Log.d(TAG, "Surface not ready for drawing")
                return
            }

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas == null) {
                    Log.e(TAG, "Canvas is null in draw method")
                    return
                }

                // Background gradient based on score
                val backgroundBlue = 0 + (score * 2).coerceAtMost(50)
                canvas.drawColor(Color.rgb(0, 0, 30 + backgroundBlue))

                // Draw special message if active
                if (showSpecialMessage) {
                    paint.color = specialColors[specialColorIndex]
                    paint.textSize = 60f
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText(currentMessage, width / 2f, height / 2f - 200, paint)
                }

                // Draw snake
                if (isSpecialSnake) {
                    paint.color = specialColors[specialColorIndex]
                } else {
                    paint.color = Color.GREEN
                }

                for (i in snakeGame.snakeBody.indices) {
                    val point = snakeGame.snakeBody[i]

                    // Skip invalid points
                    if (point.x < 0 || point.y < 0 ||
                        point.x >= width / snakeGame.blockSize.coerceAtLeast(1) ||
                        point.y >= height / snakeGame.blockSize.coerceAtLeast(1)) {
                        continue
                    }

                    // Head of snake (different color)
                    if (i == 0) {
                        paint.color = if (isSpecialSnake) specialColors[specialColorIndex] else Color.rgb(50, 205, 50)

                        canvas.drawRect(
                            (point.x * snakeGame.blockSize).toFloat(),
                            (point.y * snakeGame.blockSize).toFloat(),
                            ((point.x + 1) * snakeGame.blockSize).toFloat(),
                            ((point.y + 1) * snakeGame.blockSize).toFloat(),
                            paint
                        )

                        // Draw eyes on snake head
                        paint.color = Color.WHITE
                        val eyeSize = snakeGame.blockSize / 5f

                        // Position eyes based on direction
                        when (snakeGame.direction) {
                            SnakeGame.DIRECTION_RIGHT -> {
                                // Right-facing eyes
                                canvas.drawCircle(
                                    (point.x + 0.7f) * snakeGame.blockSize,
                                    (point.y + 0.3f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                                canvas.drawCircle(
                                    (point.x + 0.7f) * snakeGame.blockSize,
                                    (point.y + 0.7f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                            }
                            SnakeGame.DIRECTION_LEFT -> {
                                // Left-facing eyes
                                canvas.drawCircle(
                                    (point.x + 0.3f) * snakeGame.blockSize,
                                    (point.y + 0.3f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                                canvas.drawCircle(
                                    (point.x + 0.3f) * snakeGame.blockSize,
                                    (point.y + 0.7f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                            }
                            SnakeGame.DIRECTION_UP -> {
                                // Upward-facing eyes
                                canvas.drawCircle(
                                    (point.x + 0.3f) * snakeGame.blockSize,
                                    (point.y + 0.3f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                                canvas.drawCircle(
                                    (point.x + 0.7f) * snakeGame.blockSize,
                                    (point.y + 0.3f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                            }
                            SnakeGame.DIRECTION_DOWN -> {
                                // Downward-facing eyes
                                canvas.drawCircle(
                                    (point.x + 0.3f) * snakeGame.blockSize,
                                    (point.y + 0.7f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                                canvas.drawCircle(
                                    (point.x + 0.7f) * snakeGame.blockSize,
                                    (point.y + 0.7f) * snakeGame.blockSize,
                                    eyeSize, paint
                                )
                            }
                        }
                    } else {
                        // Body segments with alternating shades for effect
                        paint.color = if (isSpecialSnake) {
                            specialColors[specialColorIndex]
                        } else if (i % 2 == 0) {
                            Color.rgb(0, 180, 0)
                        } else {
                            Color.rgb(0, 155, 0)
                        }

                        canvas.drawRect(
                            (point.x * snakeGame.blockSize).toFloat(),
                            (point.y * snakeGame.blockSize).toFloat(),
                            ((point.x + 1) * snakeGame.blockSize).toFloat(),
                            ((point.y + 1) * snakeGame.blockSize).toFloat(),
                            paint
                        )
                    }
                }

                // Check if food is within valid range
                if (snakeGame.food.x >= 0 && snakeGame.food.y >= 0 &&
                    snakeGame.food.x < width / snakeGame.blockSize.coerceAtLeast(1) &&
                    snakeGame.food.y < height / snakeGame.blockSize.coerceAtLeast(1)) {

                    // Draw food with pulsing effect
                    val pulseFactor = (System.currentTimeMillis() % 1000) / 1000f
                    val pulseSize = snakeGame.blockSize / 2f + (snakeGame.blockSize / 10f) * pulseFactor

                    paint.color = Color.RED
                    val food = snakeGame.food
                    canvas.drawCircle(
                        (food.x * snakeGame.blockSize + snakeGame.blockSize / 2).toFloat(),
                        (food.y * snakeGame.blockSize + snakeGame.blockSize / 2).toFloat(),
                        pulseSize,
                        paint
                    )

                    // Draw "A" on the food for Aditya
                    paint.color = Color.WHITE
                    paint.textSize = snakeGame.blockSize * 0.8f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(
                        "A",
                        (food.x * snakeGame.blockSize + snakeGame.blockSize / 2).toFloat(),
                        (food.y * snakeGame.blockSize + snakeGame.blockSize / 2 + snakeGame.blockSize * 0.3f).toFloat(),
                        paint
                    )
                }

                // Draw the current score in the corner
                paint.color = Color.WHITE
                paint.textSize = 50f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("Aditya: $score", 20f, 60f, paint)

                if (isFirstDraw) {
                    isFirstDraw = false
                    Log.d(TAG, "First frame drawn successfully")
                }
            } finally {
                // Always unlock canvas if it was locked
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unlocking canvas: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in draw: ${e.message}")
        }
    }

    private fun control() {
        try {
            // Game speeds up as score increases, but caps at a reasonable speed
            val baseSleepTime = 150L
            val speedIncrement = score * 3L
            val finalSleepTime = (baseSleepTime - speedIncrement).coerceAtLeast(50L)
            Thread.sleep(finalSleepTime)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            Log.e(TAG, "Error in control: ${e.message}")
        }
    }

    fun pause() {
        try {
            playing = false
            try {
                gameThread?.join(500) // Add timeout to avoid hanging
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error joining game thread: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in pause: ${e.message}")
        }
    }

    fun resume() {
        try {
            // Ensure previous thread is fully stopped before starting a new one
            if (gameThread?.isAlive == true) {
                playing = false
                try {
                    gameThread?.join(1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error joining previous thread: ${e.message}")
                }
            }

            playing = true
            gameThread = Thread(this)
            gameThread?.start()
            Log.d(TAG, "Game thread started")
        } catch (e: Exception) {
            Log.e(TAG, "Error in resume: ${e.message}")
        }
    }

    fun resetGame() {
        try {
            snakeGame = SnakeGame().apply {
                setScreenSize(width, height)
            }
            score = 0
            showSpecialMessage = false
            isSpecialSnake = false
        } catch (e: Exception) {
            Log.e(TAG, "Error in resetGame: ${e.message}")
        }
    }

    private fun gameOver() {
        try {
            playing = false
            // Special game over message for Aditya
            showSpecialMessage = true
            currentMessage = if (score > 10) {
                "Amazing, Aditya! Score: $score"
            } else {
                "Good try, Aditya! Score: $score"
            }
            try {
                (context as? MainActivity)?.showRestartButton()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing restart button: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in gameOver: ${e.message}")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return try {
            gestureDetector.onTouchEvent(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onTouchEvent: ${e.message}")
            false
        }
    }

    // GestureDetector interface implementations
    override fun onDown(e: MotionEvent): Boolean = true

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,  // Non-nullable parameter matching the interface
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            if (e1 == null) return false

            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y

            // Prevent 180-degree turns (snake can't turn back on itself)
            when {
                abs(deltaX) > abs(deltaY) -> {
                    val newDirection = if (deltaX > 0) SnakeGame.DIRECTION_RIGHT else SnakeGame.DIRECTION_LEFT
                    // Don't allow right when going left, or left when going right
                    if ((snakeGame.direction == SnakeGame.DIRECTION_LEFT && newDirection == SnakeGame.DIRECTION_RIGHT) ||
                        (snakeGame.direction == SnakeGame.DIRECTION_RIGHT && newDirection == SnakeGame.DIRECTION_LEFT)) {
                        return true
                    }
                    snakeGame.direction = newDirection
                }
                else -> {
                    val newDirection = if (deltaY > 0) SnakeGame.DIRECTION_DOWN else SnakeGame.DIRECTION_UP
                    // Don't allow down when going up, or up when going down
                    if ((snakeGame.direction == SnakeGame.DIRECTION_UP && newDirection == SnakeGame.DIRECTION_DOWN) ||
                        (snakeGame.direction == SnakeGame.DIRECTION_DOWN && newDirection == SnakeGame.DIRECTION_UP)) {
                        return true
                    }
                    snakeGame.direction = newDirection
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in onFling: ${e.message}")
            return false
        }
    }
}