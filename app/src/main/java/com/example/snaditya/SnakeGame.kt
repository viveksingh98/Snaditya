// SnakeGame.kt
package com.example.snaditya

import android.graphics.Point
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.random.Random

class SnakeGame {
    companion object {
        const val DIRECTION_UP = 0
        const val DIRECTION_RIGHT = 1
        const val DIRECTION_DOWN = 2
        const val DIRECTION_LEFT = 3
    }

    var direction = DIRECTION_RIGHT
    var blockSize = 0
    private var width = 0
    private var height = 0

    val snakeBody = mutableListOf<Point>()
    var food = Point()
    private val random = Random

    init {
        // Initialize snake
        snakeBody.add(Point(5, 5))
        snakeBody.add(Point(4, 5))
        snakeBody.add(Point(3, 5))
        spawnFood()
    }

    fun setScreenSize(w: Int, h: Int) {
        width = w
        height = h
        blockSize = width / 30
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun moveSnake(): Boolean {
        val newHead = Point(snakeBody[0])
        when(direction) {
            DIRECTION_UP -> newHead.y--
            DIRECTION_DOWN -> newHead.y++
            DIRECTION_LEFT -> newHead.x--
            DIRECTION_RIGHT -> newHead.x++
        }

        if (newHead.x < 0 || newHead.x >= width/blockSize ||
            newHead.y < 0 || newHead.y >= height/blockSize) {
            return false
        }

        if (snakeBody.any { it == newHead }) return false

        snakeBody.add(0, newHead)
        snakeBody.removeLast()
        return true
    }

    fun growSnake() {
        snakeBody.add(Point(snakeBody.last()))
    }

    fun checkFoodCollision(): Boolean = snakeBody[0] == food

    fun spawnFood() {
        food.x = random.nextInt(width/blockSize)
        food.y = random.nextInt(height/blockSize)
    }
}