// SnakeGame.kt
package com.example.snaditya

import android.graphics.Point
import kotlin.random.Random

class SnakeGame {
    companion object {
        const val DIRECTION_UP = 0
        const val DIRECTION_RIGHT = 1
        const val DIRECTION_DOWN = 2
        const val DIRECTION_LEFT = 3

        // Special food types
        const val FOOD_NORMAL = 0
        const val FOOD_BONUS = 1
        const val FOOD_SPECIAL = 2
    }

    var direction = DIRECTION_RIGHT
    var blockSize = 0
    private var widthInBlocks = 0
    private var heightInBlocks = 0
    private var width = 0
    private var height = 0

    val snakeBody = mutableListOf<Point>()
    var food = Point()
    var foodType = FOOD_NORMAL
    var bonusTimer = 0
    private val random = Random

    init {
        // Initialize snake with 3 segments
        snakeBody.add(Point(5, 5))
        snakeBody.add(Point(4, 5))
        snakeBody.add(Point(3, 5))
        spawnFood()
    }

    fun setScreenSize(w: Int, h: Int) {
        width = w
        height = h
        blockSize = width / 30

        // Calculate grid size
        widthInBlocks = width / blockSize
        heightInBlocks = height / blockSize

        // If dimensions have changed, respawn food to ensure it's in bounds
        if (food.x >= widthInBlocks || food.y >= heightInBlocks) {
            spawnFood()
        }
    }

    fun moveSnake(): Boolean {
        val newHead = Point(snakeBody[0])

        // Move in current direction
        when(direction) {
            DIRECTION_UP -> newHead.y--
            DIRECTION_DOWN -> newHead.y++
            DIRECTION_LEFT -> newHead.x--
            DIRECTION_RIGHT -> newHead.x++
        }

        // Check for wall collision
        if (newHead.x < 0 || newHead.x >= widthInBlocks ||
            newHead.y < 0 || newHead.y >= heightInBlocks) {
            return false
        }

        // Check for self collision - excluding the last body part
        // (since it will move away, unless we just ate food)
        for (i in 0 until snakeBody.size - 1) {
            if (newHead.x == snakeBody[i].x && newHead.y == snakeBody[i].y) {
                return false
            }
        }

        // Add new head
        snakeBody.add(0, newHead)

        // Remove last segment
        snakeBody.removeLast()

        // Update bonus food timer if active
        if (bonusTimer > 0) {
            bonusTimer--
            if (bonusTimer == 0) {
                // Bonus food disappeared, spawn regular food
                spawnFood()
            }
        }

        return true
    }

    fun growSnake() {
        // Add a new segment at the current tail position
        if (snakeBody.isNotEmpty()) {
            snakeBody.add(Point(snakeBody.last()))
        }
    }

    fun checkFoodCollision(): Boolean {
        return snakeBody[0].x == food.x && snakeBody[0].y == food.y
    }

    fun spawnFood() {
        // Determine food type - special chance based on current snake length
        foodType = when {
            random.nextInt(100) < 5 + (snakeBody.size / 2) -> FOOD_BONUS // Bonus food more likely as snake grows
            random.nextInt(100) < 3 && snakeBody.size > 10 -> FOOD_SPECIAL // Special food only after snake length > 10
            else -> FOOD_NORMAL
        }

        // Set bonus food timer if applicable
        bonusTimer = if (foodType == FOOD_BONUS) 30 else 0

        // Keep trying to place food until we find an empty space
        var validPosition = false
        while (!validPosition) {
            food.x = random.nextInt(widthInBlocks)
            food.y = random.nextInt(heightInBlocks)

            // Make sure food is not on the snake's body
            validPosition = true
            for (segment in snakeBody) {
                if (segment.x == food.x && segment.y == food.y) {
                    validPosition = false
                    break
                }
            }

            // Try to avoid edges for nicer gameplay
            if (food.x == 0 || food.x == widthInBlocks - 1 ||
                food.y == 0 || food.y == heightInBlocks - 1) {
                if (random.nextInt(100) < 70) { // 70% chance to retry if on edge
                    validPosition = false
                }
            }
        }
    }

    // Generate obstacles for the special level
    fun generateObstacles(count: Int): List<Point> {
        val obstacles = mutableListOf<Point>()

        for (i in 0 until count) {
            var valid = false
            var attempts = 0

            while (!valid && attempts < 20) {
                attempts++

                val x = random.nextInt(2, widthInBlocks - 2)
                val y = random.nextInt(2, heightInBlocks - 2)
                val obstacle = Point(x, y)

                // Check if obstacle overlaps with snake
                var overlapsSnake = false
                for (segment in snakeBody) {
                    if (segment.x == x && segment.y == y) {
                        overlapsSnake = true
                        break
                    }
                }

                // Check if obstacle overlaps with food
                val overlapsFood = (food.x == x && food.y == y)

                // Check if obstacle overlaps with other obstacles
                var overlapsObstacles = false
                for (existingObstacle in obstacles) {
                    if (existingObstacle.x == x && existingObstacle.y == y) {
                        overlapsObstacles = true
                        break
                    }
                }

                if (!overlapsSnake && !overlapsFood && !overlapsObstacles) {
                    obstacles.add(obstacle)
                    valid = true
                }
            }
        }

        return obstacles
    }

    // Method to get food value based on type
    fun getFoodValue(): Int {
        return when (foodType) {
            FOOD_BONUS -> 3
            FOOD_SPECIAL -> 5
            else -> 1
        }
    }

    // Add Aditya's special power - teleport once through walls
    fun tryTeleport(): Boolean {
        val head = snakeBody[0]
        var teleported = false

        when {
            // Left wall teleport to right
            head.x <= 0 -> {
                val newHead = Point(widthInBlocks - 2, head.y)
                snakeBody[0] = newHead
                teleported = true
            }
            // Right wall teleport to left
            head.x >= widthInBlocks - 1 -> {
                val newHead = Point(1, head.y)
                snakeBody[0] = newHead
                teleported = true
            }
            // Top wall teleport to bottom
            head.y <= 0 -> {
                val newHead = Point(head.x, heightInBlocks - 2)
                snakeBody[0] = newHead
                teleported = true
            }
            // Bottom wall teleport to top
            head.y >= heightInBlocks - 1 -> {
                val newHead = Point(head.x, 1)
                snakeBody[0] = newHead
                teleported = true
            }
        }

        return teleported
    }
}