package com.example.snaditya

import android.graphics.Point
import android.util.Log

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

        private const val TAG = "SnakeGame"
    }

    var direction = DIRECTION_RIGHT
    var blockSize = 25 // Default value to prevent division by zero
    private var widthInBlocks = 20 // Default reasonable value
    private var heightInBlocks = 20 // Default reasonable value
    private var width = 600 // Default reasonable size
    private var height = 600 // Default reasonable size

    val snakeBody = mutableListOf<Point>()
    var food = Point(10, 10) // Default food position in middle of default grid
    private var foodType = FOOD_NORMAL
    private var bonusTimer = 0
    private var initialized = false

    init {
        try {
            Log.d(TAG, "Initializing SnakeGame with default values")
            // Initialize snake with 3 segments - use safer starting positions
            snakeBody.add(Point(10, 10)) // Head
            snakeBody.add(Point(9, 10))  // Body
            snakeBody.add(Point(8, 10))  // Tail
            // Don't spawn food in init
        } catch (e: Exception) {
            Log.e(TAG, "Error in SnakeGame initialization: ${e.message}")
        }
    }

    fun setScreenSize(w: Int, h: Int) {
        try {
            Log.d(TAG, "Setting screen size: w=$w, h=$h")

            // Validate input dimensions
            if (w <= 0 || h <= 0) {
                Log.w(TAG, "Invalid dimensions provided: w=$w, h=$h. Using defaults.")
                return
            }

            width = w
            height = h

            // Ensure reasonable block size (not too small or too large)
            blockSize = (width / 30).coerceIn(15, 50)

            // Calculate grid size
            widthInBlocks = width / blockSize
            heightInBlocks = height / blockSize

            // Ensure minimum grid size
            widthInBlocks = widthInBlocks.coerceAtLeast(10)
            heightInBlocks = heightInBlocks.coerceAtLeast(10)

            Log.d(TAG, "Grid size: ${widthInBlocks}x${heightInBlocks}, blockSize: $blockSize")

            // Ensure snake is within bounds
            validateAndFixSnakePosition()

            // Spawn food if not initialized or out of bounds
            if (!initialized || food.x >= widthInBlocks || food.y >= heightInBlocks) {
                spawnFood()
                initialized = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setScreenSize: ${e.message}")
        }
    }

    private fun validateAndFixSnakePosition() {
        // Make sure snake is within bounds
        for (segment in snakeBody) {
            if (segment.x >= widthInBlocks) segment.x = widthInBlocks - 1
            if (segment.y >= heightInBlocks) segment.y = heightInBlocks - 1
            if (segment.x < 0) segment.x = 0
            if (segment.y < 0) segment.y = 0
        }
    }

    fun moveSnake(): Boolean {
        try {
            if (snakeBody.isEmpty()) {
                Log.e(TAG, "Snake body is empty! Reinitializing snake.")
                snakeBody.add(Point(widthInBlocks / 2, heightInBlocks / 2))
                return true
            }

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

            // Check for self collision
            for (i in 0 until snakeBody.size - 1) {
                if (newHead.x == snakeBody[i].x && newHead.y == snakeBody[i].y) {
                    return false
                }
            }

            // Add new head
            snakeBody.add(0, newHead)

            // Remove last segment - using removeAt instead of removeLast for API compatibility
            if (snakeBody.size > 1) {
                snakeBody.removeAt(snakeBody.size - 1)
            }

            // Update bonus food timer if active
            if (bonusTimer > 0) {
                bonusTimer--
                if (bonusTimer == 0) {
                    spawnFood()
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in moveSnake: ${e.message}")
            return true // Return true to prevent game over on error
        }
    }

    fun growSnake() {
        try {
            // Add a new segment at the current tail position
            if (snakeBody.isNotEmpty()) {
                snakeBody.add(Point(snakeBody.last()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in growSnake: ${e.message}")
        }
    }

    fun checkFoodCollision(): Boolean {
        try {
            if (snakeBody.isEmpty()) return false
            return snakeBody[0].x == food.x && snakeBody[0].y == food.y
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkFoodCollision: ${e.message}")
            return false
        }
    }

    fun spawnFood() {
        try {
            // Validate dimensions before proceeding
            if (widthInBlocks <= 1 || heightInBlocks <= 1) {
                Log.w(TAG, "Grid too small to spawn food: ${widthInBlocks}x${heightInBlocks}")
                // Set food to a safe position
                food.x = 5
                food.y = 5
                return
            }

            // Determine food type
            foodType = FOOD_NORMAL

            bonusTimer = 0

            // Safe dimensions
            val safeWidth = widthInBlocks.coerceAtLeast(1)
            val safeHeight = heightInBlocks.coerceAtLeast(1)

            // Try to place food in valid position
            var validPosition = false
            var attempts = 0
            val maxAttempts = 50

            while (!validPosition && attempts < maxAttempts) {
                attempts++

                try {
                    // Use random with safe bounds
                    food.x = (0 until safeWidth).random()
                    food.y = (0 until safeHeight).random()
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating random food position: ${e.message}")
                    food.x = 5
                    food.y = 5
                    break
                }

                // Check if food overlaps with snake
                validPosition = true
                for (segment in snakeBody) {
                    if (segment.x == food.x && segment.y == food.y) {
                        validPosition = false
                        break
                    }
                }
            }

            // If no valid position found, place food away from snake head
            if (!validPosition) {
                if (snakeBody.isNotEmpty()) {
                    food.x = (snakeBody[0].x + safeWidth / 2) % safeWidth
                    food.y = (snakeBody[0].y + safeHeight / 2) % safeHeight
                } else {
                    // Default position
                    food.x = safeWidth / 2
                    food.y = safeHeight / 2
                }
            }

            Log.d(TAG, "Food spawned at: ${food.x},${food.y}")
        } catch (e: Exception) {
            Log.e(TAG, "Error in spawnFood: ${e.message}")
            // Set safe food position on error
            food.x = 5
            food.y = 5
        }
    }

    @Suppress("unused")
    fun getFoodValue(): Int {
        return when (foodType) {
            FOOD_BONUS -> 3
            FOOD_SPECIAL -> 5
            else -> 1
        }
    }

    @Suppress("unused")
    fun tryTeleport(): Boolean {
        try {
            if (snakeBody.isEmpty()) return false

            val head = snakeBody[0]
            var teleported = false

            when {
                head.x <= 0 -> {
                    val newHead = Point(widthInBlocks - 2, head.y)
                    snakeBody[0] = newHead
                    teleported = true
                }
                head.x >= widthInBlocks - 1 -> {
                    val newHead = Point(1, head.y)
                    snakeBody[0] = newHead
                    teleported = true
                }
                head.y <= 0 -> {
                    val newHead = Point(head.x, heightInBlocks - 2)
                    snakeBody[0] = newHead
                    teleported = true
                }
                head.y >= heightInBlocks - 1 -> {
                    val newHead = Point(head.x, 1)
                    snakeBody[0] = newHead
                    teleported = true
                }
            }

            return teleported
        } catch (e: Exception) {
            Log.e(TAG, "Error in tryTeleport: ${e.message}")
            return false
        }
    }
}