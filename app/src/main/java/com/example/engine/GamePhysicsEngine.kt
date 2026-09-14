package com.example.engine

import kotlin.math.sqrt

object GamePhysicsEngine {

    /**
     * Efficient circular collision check using squared distance to avoid expensive sqrt calculations in inner loops.
     */
    fun checkCircleCollision(x1: Float, y1: Float, r1: Float, x2: Float, y2: Float, r2: Float): Boolean {
        val dx = x1 - x2
        val dy = y1 - y2
        val minDist = r1 + r2
        return (dx * dx + dy * dy) <= (minDist * minDist)
    }

    /**
     * Calculates precise Euclidean distance between two 2D points.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Clamps a 2D position within screen boundaries with padding.
     */
    fun clampPosition(x: Float, y: Float, minX: Float, maxX: Float, minY: Float, maxY: Float): Pair<Float, Float> {
        val clampedX = x.coerceIn(minX, maxX)
        val clampedY = y.coerceIn(minY, maxY)
        return Pair(clampedX, clampedY)
    }
}
