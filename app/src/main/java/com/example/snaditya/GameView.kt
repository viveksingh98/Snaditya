// GameView.kt
package com.example.snaditya

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs

class GameView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs), Runnable, GestureDetector.OnGestureListener {

    private val holder: SurfaceHolder = getHolder()
    private val paint = Paint()
    private val gestureDetector = GestureDetector(context, this)
    private var gameThread: Thread? = null
    private var playing = false
    var score = 0
    private lateinit var snakeGame: SnakeGame

    init {
        snakeGame = SnakeGame().apply {
            setScreenSize(width, height)
        }
    }

    override fun run() {
        while (playing) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        if (!snakeGame.moveSnake()) {
            gameOver()
        }
        if (snakeGame.checkFoodCollision()) {
            score++
            snakeGame.growSnake()
            snakeGame.spawnFood()
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)

            // Draw snake
            paint.color = Color.GREEN
            for (point in snakeGame.snakeBody) {
                canvas.drawRect(
                    (point.x * snakeGame.blockSize).toFloat(),
                    (point.y * snakeGame.blockSize).toFloat(),
                    ((point.x + 1) * snakeGame.blockSize).toFloat(),
                    ((point.y + 1) * snakeGame.blockSize).toFloat(),
                    paint
                )
            }

            // Draw food
            paint.color = Color.RED
            val food = snakeGame.food
            canvas.drawCircle(
                (food.x * snakeGame.blockSize + snakeGame.blockSize / 2).toFloat(),
                (food.y * snakeGame.blockSize + snakeGame.blockSize / 2).toFloat(),
                snakeGame.blockSize / 2f,
                paint
            )

            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try {
            Thread.sleep(100 - score.coerceAtMost(80).toLong())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    fun pause() {
        playing = false
        gameThread?.join()
    }

    fun resume() {
        playing = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    fun resetGame() {
        snakeGame = SnakeGame().apply {
            setScreenSize(width, height)
        }
        score = 0
    }

    private fun gameOver() {
        playing = false
        (context as MainActivity).showRestartButton()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    // GestureDetector interface implementations
    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false

    // Fixed onScroll signature
    override fun onScroll(
        e1: MotionEvent?,  // Nullable बनाया
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y

        when {
            abs(deltaX) > abs(deltaY) -> {
                snakeGame.direction = if (deltaX > 0) SnakeGame.DIRECTION_RIGHT else SnakeGame.DIRECTION_LEFT
            }
            else -> {
                snakeGame.direction = if (deltaY > 0) SnakeGame.DIRECTION_DOWN else SnakeGame.DIRECTION_UP
            }
        }
        return true
    }
}